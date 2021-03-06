/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2019, Avicomp Services, AO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package ru.avicomp.ontapi.tests.model;

import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.*;
import ru.avicomp.ontapi.*;
import ru.avicomp.ontapi.jena.model.*;
import ru.avicomp.ontapi.jena.vocabulary.OWL;
import ru.avicomp.ontapi.jena.vocabulary.XSD;
import ru.avicomp.ontapi.utils.ReadWriteUtils;

import java.util.Collections;

/**
 * For testing miscellaneous general model functionality.
 *
 * Created by @szuev on 20.07.2018.
 */
public class MiscOntModelTest extends OntModelTestBase {

    @Test
    public void testAddAxiomWithAnonymousIndividualsInMainTripleAndAnnotation() {
        OntologyManager m = OntManagers.createONT();
        DataFactory df = m.getOWLDataFactory();
        OWLAxiom a = df.getOWLObjectPropertyAssertionAxiom(df.getOWLObjectProperty("P"),
                df.getOWLNamedIndividual("I"),
                df.getOWLAnonymousIndividual("_:b0"),
                Collections.singleton(df.getOWLAnnotation(df.getOWLAnnotationProperty("A"),
                        df.getOWLAnonymousIndividual("_:b1"))));
        OntologyModel o = m.createOntology();
        o.add(a);
        Assert.assertTrue(o.containsAxiom(a));
        o.axioms().filter(a::equals).findFirst().orElseThrow(AssertionError::new);

        o.clearCache();
        Assert.assertTrue(o.containsAxiom(a));
        o.axioms().filter(a::equals).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    public void testRemoveAxiomWithDuplicatedAnnotations() {
        OntologyModel o = OntManagers.createONT().createOntology();
        OntGraphModel g = o.asGraphModel();
        OntStatement s = g.createOntClass("X").addSubClassOfStatement(g.createOntClass("Y"));
        int duplicates = 2;
        for (int i = 0; i < duplicates; i++) {
            g.createResource(OWL.Axiom)
                    .addProperty(RDFS.comment, "XY")
                    .addProperty(OWL.annotatedProperty, s.getPredicate())
                    .addProperty(OWL.annotatedSource, s.getSubject())
                    .addProperty(OWL.annotatedTarget, s.getObject());
        }
        ReadWriteUtils.print(g);
        Assert.assertEquals(3, o.getAxiomCount());

        OWLSubClassOfAxiom owl = o.axioms(AxiomType.SUBCLASS_OF)
                .findFirst().orElseThrow(AssertionError::new);
        o.remove(owl);
        ReadWriteUtils.print(g);
        Assert.assertEquals(3, g.size());
    }

    @Test
    public void testGraphIsUnchangedInCaseOfError() {
        OntologyManager m = OntManagers.createONT();
        DataFactory df = m.getOWLDataFactory();
        OntologyModel o = m.createOntology();

        o.add(df.getOWLDeclarationAxiom(df.getOWLObjectProperty("P")));
        try {
            o.add(df.getOWLDeclarationAxiom(df.getOWLDataProperty("P")));
            Assert.fail("Possible to add punning");
        } catch (OntApiException e) {
            LOGGER.debug("Expected: '{}'", e.getMessage());
        }
        ReadWriteUtils.print(o);
        Assert.assertEquals(2, o.asGraphModel().size());
        Assert.assertEquals(1, o.axioms().count());
    }

    @Test
    public void testNaryRestrictions() {
        OntologyManager man = OntManagers.createONT();
        OntologyModel o = man.createOntology();

        OntGraphModel m = o.asGraphModel();
        OntNDP p = m.createDataProperty("p");
        OntDT d = m.getDatatype(XSD.xstring);
        OntCE.NaryDataAllValuesFrom ce1 = m.createDataAllValuesFrom(Collections.singletonList(p), d);
        OntCE.NaryDataSomeValuesFrom ce2 = m.createDataSomeValuesFrom(Collections.singletonList(p), d);
        m.createOntClass("x").addSuperClass(ce1);
        m.createOntClass("y").addEquivalentClass(ce2);
        ReadWriteUtils.print(m);

        Assert.assertEquals(5, o.axioms().peek(x -> LOGGER.debug("{}", x)).count());

        DataFactory df = man.getOWLDataFactory();
        Assert.assertTrue(o.containsAxiom(df.getOWLSubClassOfAxiom(df.getOWLClass("x"),
                df.getOWLDataAllValuesFrom(df.getOWLDataProperty("p"), df.getStringOWLDatatype()))));
        Assert.assertTrue(o.containsAxiom(df.getOWLEquivalentClassesAxiom(df.getOWLClass("y"),
                df.getOWLDataSomeValuesFrom(df.getOWLDataProperty("p"), df.getStringOWLDatatype()))));
    }

    @Test
    public void testLoadManchesterInCycle() throws OWLOntologyCreationException {
        int iter = 10;
        String input = "Prefix: o: <urn:test#>\n " +
                "Ontology: <urn:test>\n " +
                "AnnotationProperty: o:bob\n " +
                "Annotations:\n rdfs:label \"bob-label\"@en";
        OWLOntologyDocumentSource source = ReadWriteUtils.getStringDocumentSource(input, OntFormat.MANCHESTER_SYNTAX);
        for (int i = 0; i < iter; i++) {
            LOGGER.debug("Iter: #{}", (i + 1));
            OntologyManager m = OntManagers.createONT();
            OntologyModel o = m.loadOntologyFromOntologyDocument(source);
            Assert.assertEquals(2, o.axioms().peek(x -> LOGGER.debug("{}", x)).count());
        }
    }

    @Test
    public void testConcurrentLoadAndListInCycle() throws OWLOntologyCreationException {
        int iter = 10;
        OWLOntologyDocumentSource source = ReadWriteUtils.getFileDocumentSource("/ontapi/family.ttl", OntFormat.TURTLE);
        for (int i = 0; i < iter; i++) {
            LOGGER.debug("Iter: #{}", (i + 1));
            OWLOntologyManager m = OntManagers.createConcurrentONT();
            OWLOntology o = m.loadOntologyFromOntologyDocument(source);
            Assert.assertEquals(58, o.classesInSignature().peek(x -> LOGGER.debug("CL:{}", x)).count());
            Assert.assertEquals(2, o.datatypesInSignature().peek(x -> LOGGER.debug("DT:{}", x)).count());
            Assert.assertEquals(508, o.individualsInSignature().peek(x -> LOGGER.debug("NI:{}", x)).count());
            Assert.assertEquals(80, o.objectPropertiesInSignature().peek(x -> LOGGER.debug("OP:{}", x)).count());
            Assert.assertEquals(2, o.datatypesInSignature().peek(x -> LOGGER.debug("DP:{}", x)).count());
            Assert.assertEquals(1, o.annotationPropertiesInSignature().peek(x -> LOGGER.debug("AP:{}", x)).count());
            Assert.assertEquals(2845, o.axioms().peek(x -> LOGGER.debug("AXIOM:{}", x)).count());
        }
    }

    @Test
    public void testDeleteAxiomWithSharedEntity() {
        OntologyManager m = OntManagers.createONT();
        DataFactory df = m.getOWLDataFactory();
        OntologyModel o = m.createOntology();
        OWLClass c = df.getOWLClass("X");
        OWLAxiom a1 = df.getOWLSubClassOfAxiom(c, df.getOWLObjectUnionOf(df.getOWLThing(), df.getOWLNothing()));
        OWLAxiom a2 = df.getOWLDisjointClassesAxiom(c, df.getOWLThing(), df.getOWLNothing());
        OWLAxiom a3 = df.getOWLHasKeyAxiom(c, df.getOWLObjectProperty("p1"), df.getOWLObjectProperty("p2"));
        o.add(a1);
        o.add(a2);
        o.add(a3);
        ReadWriteUtils.print(o);
        Assert.assertEquals(3, o.axioms().peek(x -> LOGGER.debug("1) Axiom: {}", x)).count());

        o.remove(a1);
        ReadWriteUtils.print(o);
        Assert.assertEquals(2, o.axioms().peek(x -> LOGGER.debug("2) Axiom: {}", x)).count());

        o.clearCache();
        Assert.assertEquals(5, o.axioms().peek(x -> LOGGER.debug("3) Axiom: {}", x)).count());
    }

    @Test
    public void testLoadDisjointUnion() throws OWLOntologyCreationException {
        OWLOntologyManager m = OntManagers.createONT();
        String s = "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix xml:   <http://www.w3.org/XML/1998/namespace> .\n" +
                "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "<c1>    a       owl:Class .\n" +
                "<c4>    a       owl:Class .\n" +
                "<c2>    a       owl:Class .\n" +
                "[ a       owl:Ontology ] .\n" +
                "<c0>    a                    owl:Class ;\n" +
                "        owl:disjointUnionOf  ( <c4> <c3> ) ;\n" +
                "        owl:disjointUnionOf  ( <c2> <c1> <c1> ) .\n" +
                "<c3>    a       owl:Class .";

        OWLOntology o = m.loadOntologyFromOntologyDocument(ReadWriteUtils.getStringDocumentSource(s, OntFormat.TURTLE));
        debug(o);
        Assert.assertEquals(2, o.axioms(AxiomType.DISJOINT_UNION).count());
        Assert.assertEquals(7, o.axioms().count());
    }

    @Test
    public void testLoadHasKey() throws OWLOntologyCreationException {
        OWLOntologyManager m = OntManagers.createONT();
        String s = "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix xml:   <http://www.w3.org/XML/1998/namespace> .\n" +
                "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "<d2>    a       owl:DatatypeProperty .\n" +
                "<C>     a           owl:Class ;\n" +
                "        owl:hasKey  ( <p2> <p2> <d1> <d3> ) ;\n" +
                "        owl:hasKey  ( <p1> <p2> <d1> <d2> ) .\n" +
                "[ a       owl:Ontology ] .\n" +
                "<d3>    a       owl:ObjectProperty .\n" +
                "<p1>    a       owl:ObjectProperty .\n" +
                "<d1>    a       owl:DatatypeProperty .\n" +
                "<p2>    a       owl:ObjectProperty .";
        OWLOntology o = m.loadOntologyFromOntologyDocument(ReadWriteUtils.getStringDocumentSource(s, OntFormat.TURTLE));
        debug(o);
        Assert.assertEquals(2, o.axioms(AxiomType.HAS_KEY).count());
        Assert.assertEquals(8, o.axioms().count());
    }

    @Test
    public void testInverseOfObjectProperties() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OntManagers.createONT();
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLOntology o = manager.createOntology();
        OWLObjectProperty p1 = df.getOWLObjectProperty(IRI.create("p1"));
        OWLObjectProperty p2 = df.getOWLObjectProperty(IRI.create("p2"));
        OWLObjectInverseOf inv = df.getOWLObjectInverseOf(p1);
        o.add(df.getOWLDeclarationAxiom(p1));
        o.add(df.getOWLObjectPropertyAssertionAxiom(inv, df.getOWLNamedIndividual(IRI.create("I1")),
                df.getOWLNamedIndividual(IRI.create("I2"))));
        o.add(df.getOWLFunctionalObjectPropertyAxiom(inv));
        o.add(df.getOWLTransitiveObjectPropertyAxiom(inv));
        o.add(df.getOWLObjectPropertyAssertionAxiom(p2, df.getOWLNamedIndividual(IRI.create("I1")),
                df.getOWLNamedIndividual(IRI.create("I2"))));
        o.add(df.getOWLInverseObjectPropertiesAxiom(p2, p1));
        debug(o);

        Assert.assertEquals(3, o.axioms()
                .filter(a -> OwlObjects.objects(OWLObjectInverseOf.class, a).findAny().isPresent())
                .peek(x -> LOGGER.debug("AxiomWithInverseOf: {}", x)).count());

        Assert.assertEquals(2, ((OntologyModel) o).asGraphModel().statements(null, OWL.inverseOf, null).count());
    }

    @Test
    public void testAnonymousIndividuals() {
        OntologyManager manager = OntManagers.createONT();
        OWLDataFactory df = manager.getOWLDataFactory();
        OntologyModel o = manager.createOntology();

        OntGraphModel m = o.asGraphModel();
        OntIndividual ti1 = m.getOWLThing().createIndividual();
        OWLAnonymousIndividual ni1 = df.getOWLAnonymousIndividual();
        o.add(df.getOWLClassAssertionAxiom(df.getOWLNothing(), ni1));
        ReadWriteUtils.print(m);
        LOGGER.debug("Put: {}, {}", ti1, ni1);

        OWLAnonymousIndividual ti2 = o.classAssertionAxioms(df.getOWLThing())
                .findFirst().orElseThrow(AssertionError::new)
                .getIndividual().asOWLAnonymousIndividual();
        OntIndividual ni2 = m.getOWLNothing().individuals().findFirst().orElseThrow(AssertionError::new);
        LOGGER.debug("Get: {}, {}", ti2, ni2);

        Assert.assertEquals("_:" + ti1.getId().getLabelString(), ti2.toStringID());
        Assert.assertEquals(ni1.toStringID(), "_:" + ni2.getId().getLabelString());
    }

    @Test
    public void testRemoveStatement() {
        OntologyManager man = OntManagers.createONT();
        OntologyModel o = man.createOntology(IRI.create("X"));

        OntGraphModel m = o.asGraphModel();
        OntCE ce = m.createUnionOf(m.createOntClass("y"), m.createOntClass("z"));
        ce.createIndividual("i").attachClass(m.createOntClass("w"));
        m.createOntClass("z").addSuperClass(ce)
                .addEquivalentClass(m.createObjectAllValuesFrom(m.createObjectProperty("p"), ce));

        ReadWriteUtils.print(m);
        Assert.assertEquals(9, o.axioms().peek(x -> LOGGER.debug("1:{}", x)).count());
        Assert.assertEquals(19, m.size());

        OntStatement s = m.statements(null, OWL.unionOf, null).findFirst().orElseThrow(AssertionError::new);
        m.remove(s);

        ReadWriteUtils.print(m);
        Assert.assertEquals(6, o.axioms().peek(x -> LOGGER.debug("2:{}", x)).count());
        Assert.assertEquals(18, m.size());
    }
}
