/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2018, Avicomp Services, AO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package ru.avicomp.owlapi.tests.api.axioms;

import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import ru.avicomp.owlapi.tests.api.baseclasses.TestBase;

import static org.junit.Assert.assertTrue;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.contains;
import static ru.avicomp.owlapi.OWLFunctionalSyntaxFactory.*;

/**
 * A test case which ensures that an ontology contains entity references when
 * that ontology only contains entity declaration axioms. In other words, entity
 * declaration axioms produce the correct entity references.
 *
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group
 * @since 2.0.0
 */
@SuppressWarnings("javadoc")
public class DeclarationEntityReferencesTestCase extends TestBase {

    @Test
    public void testOWLClassDeclarationAxiom() {
        OWLClass cls = createClass();
        OWLAxiom ax = Declaration(cls);
        OWLOntology ont = getOWLOntology();
        ont.add(ax);
        assertTrue(contains(ont.classesInSignature(), cls));
    }

    @Test
    public void testOWLObjectPropertyDeclarationAxiom() {
        OWLObjectProperty prop = createObjectProperty();
        OWLAxiom ax = Declaration(prop);
        OWLOntology ont = getOWLOntology();
        ont.add(ax);
        assertTrue(contains(ont.objectPropertiesInSignature(), prop));
    }

    @Test
    public void testOWLDataPropertyDeclarationAxiom() {
        OWLDataProperty prop = createDataProperty();
        OWLAxiom ax = Declaration(prop);
        OWLOntology ont = getOWLOntology();
        ont.add(ax);
        assertTrue(contains(ont.dataPropertiesInSignature(), prop));
    }

    @Test
    public void testOWLIndividualDeclarationAxiom() {
        OWLNamedIndividual ind = createIndividual();
        OWLAxiom ax = Declaration(ind);
        OWLOntology ont = getOWLOntology();
        ont.add(ax);
        assertTrue(contains(ont.individualsInSignature(), ind));
    }
}
