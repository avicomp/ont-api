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

package ru.avicomp.ontapi.internal.axioms;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.owlapi.model.*;
import ru.avicomp.ontapi.DataFactory;
import ru.avicomp.ontapi.OntApiException;
import ru.avicomp.ontapi.internal.InternalConfig;
import ru.avicomp.ontapi.internal.InternalObjectFactory;
import ru.avicomp.ontapi.internal.ONTObject;
import ru.avicomp.ontapi.internal.objects.FactoryAccessor;
import ru.avicomp.ontapi.internal.objects.ONTClassImpl;
import ru.avicomp.ontapi.jena.model.*;
import ru.avicomp.ontapi.jena.utils.OntModels;
import ru.avicomp.ontapi.jena.vocabulary.OWL;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A translator that provides {@link OWLDisjointUnionAxiom} implementations.
 * Example in turtle:
 * <pre>{@code
 * :MyClass1 owl:disjointUnionOf ( :MyClass2 [ a owl:Class ; owl:unionOf ( :MyClass3 :MyClass4  ) ] ) ;
 * }</pre>
 * <p>
 * Created by @szuev on 17.10.2016.
 *
 * @see <a href='https://www.w3.org/TR/owl2-syntax/#Disjoint_Union_of_Class_Expressions'>9.1.4 Disjoint Union of Class Expressions</a>
 */
public class DisjointUnionTranslator extends AbstractListBasedTranslator<OWLDisjointUnionAxiom, OntClass,
        OWLClassExpression, OntCE, OWLClassExpression> {
    @Override
    public OWLObject getSubject(OWLDisjointUnionAxiom axiom) {
        return axiom.getOWLClass();
    }

    @Override
    public Property getPredicate() {
        return OWL.disjointUnionOf;
    }

    @Override
    public Collection<? extends OWLObject> getObjects(OWLDisjointUnionAxiom axiom) {
        return axiom.getOperandsAsList();
    }

    @Override
    Class<OntClass> getView() {
        return OntClass.class;
    }

    @Override
    public ONTObject<OWLDisjointUnionAxiom> toAxiom(OntStatement statement,
                                                    Supplier<OntGraphModel> model,
                                                    InternalObjectFactory factory,
                                                    InternalConfig config) {
        return AxiomImpl.create(statement, model, factory, config);
    }

    @Override
    public ONTObject<OWLDisjointUnionAxiom> toAxiom(OntStatement statement,
                                                    InternalObjectFactory factory,
                                                    InternalConfig config) {
        return makeAxiom(statement, factory::getClass, OntClass::findDisjointUnion, factory::getClass, Collectors.toSet(),
                (s, m) -> factory.getOWLDataFactory().getOWLDisjointUnionAxiom(s.getOWLObject().asOWLClass(),
                        ONTObject.toSet(m),
                        ONTObject.toSet(factory.getAnnotations(statement, config))));
    }

    /**
     * @see ru.avicomp.ontapi.owlapi.axioms.OWLDisjointUnionAxiomImpl
     */
    @SuppressWarnings("WeakerAccess")
    public static class AxiomImpl
            extends WithListImpl<OWLDisjointUnionAxiom, OntCE>
            implements WithList.Sorted<OWLDisjointUnionAxiom, OWLClass, OWLClassExpression>, OWLDisjointUnionAxiom {

        private static final BiFunction<Triple, Supplier<OntGraphModel>, AxiomImpl> FACTORY = AxiomImpl::new;

        protected AxiomImpl(Triple t, Supplier<OntGraphModel> m) {
            super(t, m);
        }

        protected AxiomImpl(Object subject, String predicate, Object object, Supplier<OntGraphModel> m) {
            super(subject, predicate, object, m);
        }

        /**
         * Creates an {@link ONTObject} container that is also {@link  OWLDisjointUnionAxiom}.
         *
         * @param statement {@link OntStatement}, not {@code null}
         * @param model     {@link OntGraphModel} provider, not {@code null}
         * @param factory   {@link InternalObjectFactory}, not {@code null}
         * @param config    {@link InternalConfig}, not {@code null}
         * @return {@link AxiomImpl}
         */
        public static AxiomImpl create(OntStatement statement,
                                       Supplier<OntGraphModel> model,
                                       InternalObjectFactory factory,
                                       InternalConfig config) {
            return WithList.Sorted.create(statement, model, FACTORY, SET_HASH_CODE, factory, config);
        }

        @Override
        protected OntList<OntCE> findList(OntStatement statement) {
            return statement.getSubject(OntClass.class).findDisjointUnion(statement.getObject(RDFList.class))
                    .orElseThrow(() -> new OntApiException.IllegalState("Can't find []-list in " + statement));
        }

        @Override
        public ExtendedIterator<ONTObject<? extends OWLClassExpression>> listONTComponents(OntStatement statement,
                                                                                           InternalObjectFactory factory) {
            return OntModels.listMembers(findList(statement)).mapWith(factory::getClass);
        }

        @Override
        public OWLClass getOWLClass() {
            return getONTSubject().getOWLObject();
        }

        @Override
        public Stream<OWLClassExpression> classExpressions() {
            return operands();
        }

        @Override
        public Stream<OWLClassExpression> operands() {
            return members().map(ONTObject::getOWLObject);
        }

        @Override
        public ONTObject fromContentItem(Object x, InternalObjectFactory factory) {
            return x instanceof String ? findSubjectByURI((String) x, factory) : (ONTObject) x;
        }

        @Override
        public ONTObject<OWLClass> findSubjectByURI(String uri, InternalObjectFactory factory) {
            return ONTClassImpl.find(uri, factory, model);
        }

        @Override
        public ONTObject<OWLClass> fetchONTSubject(OntStatement statement,
                                                   InternalObjectFactory factory) {
            return findSubjectByURI(statement.getSubject().getURI(), factory);
        }

        @Override
        protected AxiomImpl makeCopy(ONTObject<OWLDisjointUnionAxiom> other) {
            return new AxiomImpl(subject, predicate, object, model) {
                @Override
                public Stream<Triple> triples() {
                    return Stream.concat(AxiomImpl.this.triples(), other.triples());
                }
            };
        }

        @FactoryAccessor
        @Override
        protected OWLDisjointUnionAxiom createAnnotatedAxiom(Object[] content,
                                                             InternalObjectFactory factory,
                                                             Collection<OWLAnnotation> annotations) {
            return getDataFactory().getOWLDisjointUnionAxiom(getFactoryClass(content, factory),
                    getFactoryMembers(content, factory), annotations);
        }

        @FactoryAccessor
        @Override
        public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom() {
            DataFactory df = getDataFactory();
            InternalObjectFactory factory = getObjectFactory();
            Object[] content = getContent();
            return df.getOWLEquivalentClassesAxiom(getFactoryClass(content, factory),
                    df.getOWLObjectUnionOf(getFactoryMembers(content, factory)));
        }

        @FactoryAccessor
        @Override
        public OWLDisjointClassesAxiom getOWLDisjointClassesAxiom() {
            return getDataFactory().getOWLDisjointClassesAxiom(getFactoryMembers(getContent(), getObjectFactory()));
        }

        @FactoryAccessor
        protected List<OWLClassExpression> getFactoryMembers(Object[] content, InternalObjectFactory factory) {
            return members(content, factory).map(x -> eraseModel(x.getOWLObject())).collect(Collectors.toList());
        }

        @FactoryAccessor
        protected OWLClass getFactoryClass(Object[] content, InternalObjectFactory factory) {
            return eraseModel(findONTSubject(content[0], factory).getOWLObject());
        }
    }
}
