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
import org.semanticweb.owlapi.model.*;
import ru.avicomp.ontapi.internal.InternalConfig;
import ru.avicomp.ontapi.internal.InternalObjectFactory;
import ru.avicomp.ontapi.internal.ONTObject;
import ru.avicomp.ontapi.internal.objects.*;
import ru.avicomp.ontapi.jena.model.OntGraphModel;
import ru.avicomp.ontapi.jena.model.OntStatement;
import ru.avicomp.ontapi.owlapi.OWLObjectImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A technical interface that describes an assertion axiom.
 * There are three positive property assertion statements ({@code a1 PN a2}, {@code a R v}, {@code s A t}),
 * and two negative property assertions:
 * <pre>{@code
 * _:x rdf:type owl:NegativePropertyAssertion.
 * _:x owl:sourceIndividual a1.
 * _:x owl:assertionProperty P.
 * _:x owl:targetIndividual a2.
 * }</pre>, and
 * <pre>{@code
 * _:x rdf:type owl:NegativePropertyAssertion.
 * _:x owl:sourceIndividual a.
 * _:x owl:assertionProperty R.
 * _:x owl:targetValue v.
 * }</pre>.
 * Where:
 * <ul>
 *     <li>{@code P} - object property expression</li>
 *     <li>{@code PN} - (named) object property </li>
 *     <li>{@code R} - data property</li>
 *     <li>{@code A} - annotation property</li>
 *     <li>{@code a} - individual (named or anonymous)</li>
 *     <li>{@code v} - literal</li>
 *     <li>{@code s} - either an IRI or an anonymous individual</li>
 *     <li>{@code t} - IRI, anonymous individual, or literal</li>
 * </ul>
 * Created by @szz on 08.10.2019.
 *
 * @param <S> - subtype of {@link OWLObject}
 * @param <P> - subtype of {@link OWLObject}
 *            (that be {@link OWLProperty}; it is not restricted due to OWL-API interface limitations)
 * @param <O> - subtype of {@link OWLObject}
 * @since 1.4.3
 */
interface WithAssertion<S extends OWLObject, P extends OWLObject, O extends OWLObject>
        extends WithTriple, HasSubject<S>, HasProperty<P> {

    ONTObject<? extends S> findONTSubject(InternalObjectFactory factory);

    ONTObject<? extends P> findONTPredicate(InternalObjectFactory factory);

    ONTObject<? extends O> findONTObject(InternalObjectFactory factory);

    default ONTObject<? extends S> fetchONTSubject(OntStatement statement, InternalObjectFactory factory) {
        return findONTSubject(factory);
    }

    default ONTObject<? extends P> fetchONTPredicate(OntStatement statement, InternalObjectFactory factory) {
        return findONTPredicate(factory);
    }

    default ONTObject<? extends O> fetchONTObject(OntStatement statement, InternalObjectFactory factory) {
        return findONTObject(factory);
    }

    default S getSubject() {
        return getONTSubject().getOWLObject();
    }

    default P getProperty() {
        return getONTPredicate().getOWLObject();
    }

    default O getValue() {
        return getONTObject().getOWLObject();
    }

    default ONTObject<? extends S> getONTSubject() {
        return findONTSubject(getObjectFactory());
    }

    default ONTObject<? extends P> getONTPredicate() {
        return findONTPredicate(getObjectFactory());
    }

    default ONTObject<? extends O> getONTObject() {
        return findONTObject(getObjectFactory());
    }

    @FactoryAccessor
    default S getFSubject() {
        return ONTObjectImpl.eraseModel(getSubject());
    }

    @FactoryAccessor
    default P getFPredicate() {
        return ONTObjectImpl.eraseModel(getProperty());
    }

    @FactoryAccessor
    default O getFObject() {
        return ONTObjectImpl.eraseModel(getValue());
    }

    @Override
    default Set<? extends OWLObject> getOWLComponentsAsSet(InternalObjectFactory factory) {
        Set<OWLObject> res = OWLObjectImpl.createSortedSet();
        res.add(findONTSubject(factory).getOWLObject());
        res.add(findONTPredicate(factory).getOWLObject());
        res.add(findONTObject(factory).getOWLObject());
        return res;
    }

    @Override
    default Stream<ONTObject<? extends OWLObject>> objects() {
        InternalObjectFactory factory = getObjectFactory();
        return Stream.of(findONTSubject(factory), findONTPredicate(factory), findONTObject(factory));
    }

    /**
     * Creates an {@link ONTObject} container for the given {@link OntStatement};
     * the returned object is also {@link R} - a positive property assertion axiom.
     * Impl notes:
     * If there is no sub-annotations then a simplified instance of {@link Simple} is returned,
     * and for this the factory {@code simple} is used.
     * Otherwise the instance is {@link WithAnnotations}
     * and it is created by the factory {@code complex} and has a cache inside.
     * Note: this is an auxiliary method as shortcut to reduce copy-pasting, it is for internal usage only.
     *
     * @param statement {@link OntStatement}, the source to parse, not {@code null}
     * @param model     {@link OntGraphModel}-provider, not {@code null}
     * @param simple    factory (as {@link BiFunction}) to provide {@link Simple} instance, not {@code null}
     * @param complex   factory (as {@link BiFunction}) to provide {@link WithAnnotations} instance, not {@code null}
     * @param setHash   {@code ObjIntConsumer<OWLAxiom>}, facility to assign {@code hashCode}, not {@code null}
     * @param factory   {@link InternalObjectFactory} (singleton), not {@code null}
     * @param config    {@link InternalConfig} (singleton), not {@code null}
     * @param <R>       the desired positive property assertion {@link OWLAxiom axiom}-type
     * @return {@link R}
     */
    static <R extends ONTObject & WithAssertion> R create(OntStatement statement,
                                                          Supplier<OntGraphModel> model,
                                                          BiFunction<Triple, Supplier<OntGraphModel>, ? extends R> simple,
                                                          BiFunction<Triple, Supplier<OntGraphModel>, ? extends R> complex,
                                                          ObjIntConsumer<OWLAxiom> setHash,
                                                          InternalObjectFactory factory,
                                                          InternalConfig config) {
        R s = simple.apply(statement.asTriple(), model);
        Object[] content = WithAnnotations.initContent(s, statement, setHash, factory, config);
        if (content == null) {
            return s;
        }
        R c = complex.apply(statement.asTriple(), model);
        setHash.accept(c, s.hashCode());
        ((WithContent<?>) c).putContent(content);
        return c;
    }

    /**
     * Creates an {@link ONTObject} container for the given {@link OntStatement};
     * the returned object is also {@link R} - a negative property assertion axiom.
     * <p>
     * For internal usage only.
     *
     * @param statement {@link OntStatement}, the source to parse, not {@code null}
     * @param model     {@link OntGraphModel}-provider, not {@code null}
     * @param maker     a factory (as {@link BiFunction}) to provide {@link Complex} instance, not {@code null}
     * @param setHash   {@code ObjIntConsumer<OWLAxiom>}, facility to assign {@code hashCode}, not {@code null}
     * @param factory   {@link InternalObjectFactory} (singleton), not {@code null}
     * @param config    {@link InternalConfig} (singleton), not {@code null}
     * @param <R>       the desired negative property assertion {@link OWLAxiom axiom}-type
     * @return {@link R}
     */
    static <R extends ONTObject & Complex> R create(OntStatement statement,
                                                    Supplier<OntGraphModel> model,
                                                    BiFunction<Triple, Supplier<OntGraphModel>, ? extends R> maker,
                                                    ObjIntConsumer<OWLAxiom> setHash,
                                                    InternalObjectFactory factory,
                                                    InternalConfig config) {
        R res = maker.apply(statement.asTriple(), model);
        Object[] content = Complex.initContent(res, statement, setHash, factory, config);
        res.putContent(content);
        return res;
    }

    /**
     * Represents the simplest case when the axiom has no annotations.
     *
     * @param <S> - subject
     * @param <P> - predicate
     * @param <O> - object
     */
    interface Simple<S extends OWLObject, P extends OWLObject, O extends OWLObject>
            extends WithAssertion<S, P, O>, WithoutAnnotations {

        @Override
        default boolean isAnnotated() {
            return false;
        }
    }

    /**
     * Represents the case of annotated positive assertion axiom.
     * The {@link WithContent#getContent()} contains only annotations.
     *
     * @param <A> - assertion
     * @param <S> - subject
     * @param <P> - predicate
     * @param <O> - object
     */
    interface WithAnnotations<A extends OWLAxiom, S extends OWLObject, P extends OWLObject, O extends OWLObject>
            extends WithAssertion<S, P, O>, WithContent<A> {

        /**
         * Calculates the content and {@code hashCode} simultaneously.
         * Such a way was chosen for performance sake.
         *
         * @param axiom     - a {@link WithAnnotations} instance, the axiom, not {@code null}
         * @param statement - a {@link OntStatement}, the source statement, not {@code null}
         * @param setHash   - a {@code ObjIntConsumer<OWLAxiom>}, facility to assign {@code hashCode}, not {@code null}
         *                  (no annotations, uri subject and object), an empty array is returned
         * @param factory   - a {@link InternalObjectFactory} singleton, not {@code null}
         * @param config    - a {@link InternalConfig} singleton, not {@code null}
         * @return an {@code Array} with content or {@code null} if no content is needed
         */
        static Object[] initContent(WithAssertion axiom,
                                    OntStatement statement,
                                    ObjIntConsumer<OWLAxiom> setHash,
                                    InternalObjectFactory factory,
                                    InternalConfig config) {
            int hash = OWLObject.hashIteration(axiom.hashIndex(), axiom.fetchONTSubject(statement, factory).hashCode());
            hash = OWLObject.hashIteration(hash, axiom.fetchONTPredicate(statement, factory).hashCode());
            hash = OWLObject.hashIteration(hash, axiom.fetchONTObject(statement, factory).hashCode());
            Collection annotations = ONTAxiomImpl.collectAnnotations(statement, factory, config);
            Object[] res = new Object[annotations.size()];
            int h = 1, index = 0;
            for (Object a : annotations) {
                res[index++] = a;
                h = WithContent.hashIteration(h, a.hashCode());
            }
            setHash.accept(axiom, OWLObject.hashIteration(hash, h));
            return res.length == 0 ? null : res;
        }

        @Override
        default Object[] collectContent() {
            OntStatement statement = asStatement();
            InternalObjectFactory factory = getObjectFactory();
            InternalConfig config = getConfig();
            return ONTAxiomImpl.collectAnnotations(statement, factory, config).toArray();
        }

        @Override
        default boolean isAnnotated() {
            return true;
        }

        @Override
        default Stream<OWLAnnotation> annotations() {
            return ONTAnnotationImpl.contentAsStream(getContent());
        }

        @Override
        default List<OWLAnnotation> annotationsAsList() {
            return ONTAnnotationImpl.contentAsList(getContent());
        }

        @SuppressWarnings("unchecked")
        @Override
        default Stream<ONTObject<? extends OWLObject>> objects() {
            Stream res = Stream.concat(WithAssertion.super.objects(), annotations());
            return (Stream<ONTObject<? extends OWLObject>>) res;
        }
    }

    /**
     * Represents a negative assertion axiom.
     * The {@link WithContent#getContent()} contains both annotations and part of axiom.
     *
     * @param <A> - assertion
     * @param <S> - subject
     * @param <P> - predicate
     * @param <O> - object
     */
    interface Complex<A extends OWLAxiom, S extends OWLObject, P extends OWLObject, O extends OWLObject>
            extends WithAssertion<S, P, O>, WithContent<A> {

        /**
         * Calculates the content and {@code hashCode} simultaneously.
         * Such a way was chosen for performance sake.
         *
         * @param axiom     - a {@link Complex} instance, the axiom, not {@code null}
         * @param statement - a {@link OntStatement}, the source statement, not {@code null}
         * @param setHash   - a {@code ObjIntConsumer<OWLAxiom>}, facility to assign {@code hashCode}, not {@code null}
         *                  (no annotations, uri subject and object), an empty array is returned
         * @param factory   - a {@link InternalObjectFactory} singleton, not {@code null}
         * @param config    - a {@link InternalConfig} singleton, not {@code null}
         * @return an {@code Array} with content
         */
        static Object[] initContent(Complex axiom,
                                    OntStatement statement,
                                    ObjIntConsumer<OWLAxiom> setHash,
                                    InternalObjectFactory factory,
                                    InternalConfig config) {
            Collection annotations = ONTAxiomImpl.collectAnnotations(statement, factory, config);
            Object[] res = new Object[annotations.size() + 3];
            int hash = axiom.hashIndex();
            ONTObject s = axiom.fetchONTSubject(statement, factory);
            ONTObject p = axiom.fetchONTPredicate(statement, factory);
            ONTObject o = axiom.fetchONTObject(statement, factory);
            hash = OWLObject.hashIteration(hash, s.hashCode());
            hash = OWLObject.hashIteration(hash, p.hashCode());
            hash = OWLObject.hashIteration(hash, o.hashCode());
            int i = 0;
            res[i++] = axiom.fromSubject(s);
            res[i++] = axiom.fromPredicate(p);
            res[i++] = axiom.fromObject(o);
            int h = 1;
            for (Object a : annotations) {
                res[i++] = a;
                h = WithContent.hashIteration(h, a.hashCode());
            }
            setHash.accept(axiom, OWLObject.hashIteration(hash, h));
            return res;
        }

        @Override
        default Object[] collectContent() {
            OntStatement statement = asStatement();
            InternalObjectFactory factory = getObjectFactory();
            InternalConfig config = getConfig();
            Collection annotations = ONTAxiomImpl.collectAnnotations(statement, factory, config);
            Object[] res = new Object[annotations.size() + 3];
            int i = 0;
            res[i++] = fromSubject(fetchONTSubject(statement, factory));
            res[i++] = fromPredicate(fetchONTPredicate(statement, factory));
            res[i++] = fromObject(fetchONTObject(statement, factory));
            for (Object a : annotations) {
                res[i++] = a;
            }
            return res;
        }

        Object fromSubject(ONTObject o);

        Object fromObject(ONTObject o);

        Object fromPredicate(ONTObject o);

        ONTObject<? extends S> toSubject(Object s, InternalObjectFactory factory);

        ONTObject<? extends P> toPredicate(Object p, InternalObjectFactory factory);

        ONTObject<? extends O> toObject(Object o, InternalObjectFactory factory);

        default ONTObject<? extends S> findONTSubject(InternalObjectFactory factory) {
            return toSubject(getContent()[0], factory);
        }

        default ONTObject<? extends P> findONTPredicate(InternalObjectFactory factory) {
            return toPredicate(getContent()[1], factory);
        }

        default ONTObject<? extends O> findONTObject(InternalObjectFactory factory) {
            return toObject(getContent()[2], factory);
        }

        @Override
        default boolean isAnnotated() {
            return getContent().length > 3;
        }

        @Override
        default Stream<OWLAnnotation> annotations() {
            return annotations(getContent());
        }

        @Override
        default List<OWLAnnotation> annotationsAsList() {
            return annotations().collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        default Stream<OWLAnnotation> annotations(Object[] content) {
            Stream res = Arrays.stream(content).skip(3);
            return (Stream<OWLAnnotation>) res;
        }

        default Stream<ONTObject<? extends OWLObject>> objects(Object[] content, InternalObjectFactory factory) {
            return Stream.of(toSubject(content[0], factory), toPredicate(content[1], factory),
                    toObject(content[2], factory));
        }

        @SuppressWarnings("unchecked")
        @Override
        default Stream<ONTObject<? extends OWLObject>> objects() {
            Object[] content = getContent();
            InternalObjectFactory factory = getObjectFactory();
            Stream res = Stream.concat(objects(content, factory), annotations(content));
            return (Stream<ONTObject<? extends OWLObject>>) res;
        }

        @Override
        default Set<? extends OWLObject> getOWLComponentsAsSet(InternalObjectFactory factory) {
            return getOWLComponentsAsSet(getContent(), factory);
        }

        default Set<? extends OWLObject> getOWLComponentsAsSet(Object[] content, InternalObjectFactory factory) {
            Set<OWLObject> res = OWLObjectImpl.createSortedSet();
            objects(content, factory).map(ONTObject::getOWLObject).forEach(res::add);
            return res;
        }
    }
}
