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

package ru.avicomp.ontapi.jena.model;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import ru.avicomp.ontapi.jena.OntJenaException;
import ru.avicomp.ontapi.jena.vocabulary.OWL;
import ru.avicomp.ontapi.jena.vocabulary.RDF;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for named and anonymous individuals.
 * <p>
 * Created by @szuev on 02.11.2016.
 */
public interface OntIndividual extends OntObject {

    /**
     * Removes a class assertion statement for the given class.
     * Like others methods {@code #remove..(..)} (see {@link #remove(Property, RDFNode)}),
     * this operation does nothing in case no match found
     * and in case {@code null} is specified
     * it removes all class assertion statements including all their annotations.
     * To delete the individual with its content
     * the the method {@link OntGraphModel#removeOntObject(OntObject)} can be used.
     *
     * @param clazz {@link OntCE} or {@code null} to remove all class assertions
     * @return <b>this</b> instance to allow cascading calls
     * @see #attachClass(OntCE)
     * @see #addClassAssertion(OntCE)
     * @see OntGraphModel#removeOntObject(OntObject)
     */
    OntIndividual detachClass(Resource clazz);

    /**
     * Answers a {@code Stream} over the class expressions to which this individual belongs,
     * including super-classes if the flag {@code direct} is {@code false}.
     * If the flag {@code direct} is {@code true}, then only direct types are returned,
     * and the method is effectively equivalent to the method {@link #classes()}.
     * See also {@link OntCE#superClasses(boolean)}.
     *
     * @param direct if {@code true}, only answers those {@link OntCE}s that are direct types of this individual,
     *               not the super-classes of the class etc
     * @return <b>distinct</b> {@code Stream} of {@link OntCE class expressions}
     * @see #classes()
     * @see OntCE#superClasses(boolean)
     * @since 1.4.0
     */
    Stream<OntCE> classes(boolean direct);

    /**
     * {@inheritDoc}
     * For individuals content also includes negative property assertion statements.
     *
     * @return {@code Stream} of content {@link OntStatement}s
     */
    @Override
    Stream<OntStatement> content();

    /**
     * Returns all direct class types.
     *
     * @return {@code Stream} of {@link OntCE}s
     * @since 1.4.0
     */
    default Stream<OntCE> classes() {
        return objects(RDF.type, OntCE.class);
    }

    /**
     * Lists all same individuals.
     * The pattern to search for is {@code ai owl:sameAs aj}, where {@code ai} is this individual.
     *
     * @return {@code Stream} of {@link OntIndividual}s
     * @since 1.4.0
     */
    default Stream<OntIndividual> sameIndividuals() {
        return objects(OWL.sameAs, OntIndividual.class);
    }

    /**
     * Lists all different individuals.
     * The pattern to search for is {@code a1 owl:differentFrom a2},
     * where {@code a1} is this {@link OntIndividual} and {@code a2} is one of the returned {@link OntIndividual}.
     *
     * @return {@code Stream} of {@link OntIndividual}s
     * @see OntDisjoint.Individuals
     * @since 1.4.0
     */
    default Stream<OntIndividual> differentIndividuals() {
        return objects(OWL.differentFrom, OntIndividual.class);
    }

    /**
     * Lists all positive assertions for this individual.
     *
     * @return {@code Stream} of {@link OntStatement}s
     */
    default Stream<OntStatement> positiveAssertions() {
        return statements().filter(s -> s.getPredicate().canAs(OntProperty.class));
    }

    /**
     * Lists all positive property assertions for this individual and the given predicate.
     *
     * @param predicate {@link OntProperty} or {@code null}
     * @return {@code Stream} of {@link OntStatement}s
     * @since 1.4.0
     */
    default Stream<OntStatement> positiveAssertions(OntProperty predicate) {
        return statements(predicate);
    }

    /**
     * Lists all negative property assertions for this individual.
     *
     * @return {@code Stream} of {@link OntNPA negative property assertion}s
     */
    default Stream<OntNPA> negativeAssertions() {
        return getModel().statements(null, OWL.sourceIndividual, this)
                .map(x -> x.getSubject().getAs(OntNPA.class))
                .filter(Objects::nonNull);
    }

    /**
     * Lists all negative property assertions for this individual and the given property.
     *
     * @param property {@link OntProperty} or {@code null}
     * @return {@code Stream} of {@link OntNPA negative property assertion}s
     * @since 1.4.0
     */
    default Stream<OntNPA> negativeAssertions(OntProperty property) {
        return negativeAssertions().filter(x -> property == null || x.getProperty().equals(property));
    }

    /**
     * Creates and returns a class-assertion statement {@code a rdf:type C}, where {@code a} is this individual.
     *
     * @param clazz {@link OntCE}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #attachClass(OntCE)
     * @see #detachClass(Resource)
     * @since 1.4.0
     */
    default OntStatement addClassAssertion(OntCE clazz) {
        return addStatement(RDF.type, clazz);
    }

    /**
     * Adds a {@link OWL#differentFrom owl:differentFrom} individual statement.
     *
     * @param other {@link OntIndividual}, not {@code null}
     * @return {@link OntStatement} to provide the ability to add annotations subsequently
     * @see #addDifferentIndividual(OntIndividual)
     * @see #removeDifferentIndividual(Resource)
     * @see OntDisjoint.Individuals
     * @since 1.4.0
     */
    default OntStatement addDifferentFromStatement(OntIndividual other) {
        return addStatement(OWL.differentFrom, other);
    }

    /**
     * Adds a same individual reference.
     *
     * @param other {@link OntIndividual}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @see #addSameIndividual(OntIndividual)
     * @see #removeSameIndividual(Resource)
     * @see <a href='https://www.w3.org/TR/owl2-syntax/#Individual_Equality'>9.6.1 Individual Equality</a>
     * @since 1.4.0
     */
    default OntStatement addSameAsStatement(OntIndividual other) {
        return addStatement(OWL.sameAs, other);
    }

    /**
     * Adds a type (class expression) to this individual.
     *
     * @param clazz {@link OntCE}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addClassAssertion(OntCE)
     * @see #detachClass(Resource)
     */
    default OntIndividual attachClass(OntCE clazz) {
        addClassAssertion(clazz);
        return this;
    }

    /**
     * Adds a {@link OWL#differentFrom owl:differentFrom} individual statement
     * and returns this object itself to allow cascading calls.
     *
     * @param other {@link OntIndividual}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDifferentFromStatement(OntIndividual)
     * @see #removeDifferentIndividual(Resource)
     * @see OntDisjoint.Individuals
     * @see <a href='https://www.w3.org/TR/owl2-syntax/#Individual_Inequality'>9.6.2 Individual Inequality</a>
     * @since 1.4.0
     */
    default OntIndividual addDifferentIndividual(OntIndividual other) {
        addDifferentFromStatement(other);
        return this;
    }

    /**
     * Adds a {@link OWL#sameAs owl:sameAs} individual statement
     * and returns this object itself to allow cascading calls.
     *
     * @param other other {@link OntIndividual}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSameAsStatement(OntIndividual)
     * @see #removeSameIndividual(Resource)
     * @since 1.4.0
     */
    default OntIndividual addSameIndividual(OntIndividual other) {
        addSameAsStatement(other);
        return this;
    }

    /**
     * Adds annotation assertion {@code AnnotationAssertion(A s t)}.
     * In general case it is {@code s A t}, where {@code s} is IRI or anonymous individual,
     * {@code A} - annotation property, and {@code t} - IRI, anonymous individual, or literal.
     *
     * @param property {@link OntNAP}
     * @param value    {@link RDFNode} (IRI, anonymous individual, or literal)
     * @return this individual to allow cascading calls
     * @see #addAnnotation(OntNAP, RDFNode)
     * @see #removeAssertion(OntProperty, RDFNode)
     */
    default OntIndividual addAssertion(OntNAP property, RDFNode value) {
        return addProperty(property, value);
    }

    /**
     * Adds a positive data property assertion {@code a R v}.
     *
     * @param property {@link OntNDP}
     * @param value    {@link Literal}
     * @return this individual to allow cascading calls
     * @see #removeAssertion(OntProperty, RDFNode)
     */
    default OntIndividual addAssertion(OntNDP property, Literal value) {
        return addProperty(property, value);
    }

    /**
     * Adds a positive object property assertion {@code a1 PN a2}.
     *
     * @param property {@link OntNOP} named object property
     * @param value    {@link OntIndividual} other individual
     * @return this individual to allow cascading calls
     * @see #removeAssertion(OntProperty, RDFNode)
     */
    default OntIndividual addAssertion(OntNOP property, OntIndividual value) {
        return addProperty(property, value);
    }

    /**
     * Adds a property assertion statement.
     * <b>Caution</b>: this method offers a way to add a statement that is contrary to the OWL2 specification.
     * For example, it is possible to add {@link OntNOP object property}-{@link Literal literal} pair,
     * that is not object property assertion.
     *
     * @param property {@link OntProperty}, not {@code null}
     * @param value    {@link RDFNode}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see Resource#addProperty(Property, RDFNode)
     * @see #removeAssertion(OntProperty, RDFNode)
     * @since 1.4.0
     */
    default OntIndividual addProperty(OntProperty property, RDFNode value) {
        addStatement(property, value);
        return this;
    }

    /**
     * Adds a negative object property assertion.
     * <pre>
     * Functional syntax: {@code NegativeObjectPropertyAssertion(P a1 a2)}
     * RDF Syntax:
     * {@code
     * _:x rdf:type owl:NegativePropertyAssertion .
     * _:x owl:sourceIndividual a1 .
     * _:x owl:assertionProperty P .
     * _:x owl:targetIndividual a2 .
     * }
     * </pre>
     *
     * @param property {@link OntOPE}
     * @param value    {@link OntIndividual} other individual
     * @return <b>this</b> individual to allow cascading calls
     */
    default OntIndividual addNegativeAssertion(OntOPE property, OntIndividual value) {
        property.addNegativeAssertion(this, value);
        return this;
    }

    /**
     * Adds a negative data property assertion.
     * <pre>
     * Functional syntax: {@code NegativeDataPropertyAssertion(R a v)}
     * RDF Syntax:
     * {@code
     * _:x rdf:type owl:NegativePropertyAssertion.
     * _:x owl:sourceIndividual a .
     * _:x owl:assertionProperty R .
     * _:x owl:targetValue v .
     * }
     * </pre>
     *
     * @param property {@link OntNDP}
     * @param value    {@link Literal}
     * @return <b>this</b> individual to allow cascading calls
     */
    default OntIndividual addNegativeAssertion(OntNDP property, Literal value) {
        property.addNegativeAssertion(this, value);
        return this;
    }

    /**
     * Removes a positive property assertion including its annotation.
     *
     * @param property {@link OntProperty}, can be {@code null} to remove all positive property assertions
     * @param value    {@link RDFNode} (either {@link OntIndividual} or {@link Literal}),
     *                 can be {@code null} to remove all assertions for the predicate {@code property}
     * @return <b>this</b> instance to allow cascading calls
     * @see OntObject#remove(Property, RDFNode)
     * @see #addProperty(OntProperty, RDFNode)
     * @since 1.4.0
     */
    default OntIndividual removeAssertion(OntProperty property, RDFNode value) {
        statements(property)
                .filter(x -> x.getPredicate().canAs(OntProperty.class)
                        && (value == null || value.equals(x.getObject())))
                .collect(Collectors.toList()).forEach(x -> x.getModel().remove(x.clearAnnotations()));
        return this;
    }

    /**
     * Removes a negative property assertion including its annotation.
     *
     * @param property {@link OntProperty}, can be {@code null} to remove all negative property assertions
     * @param value    {@link RDFNode} (either {@link OntIndividual} or {@link Literal}),
     *                 can be {@code null} to remove all assertions for the predicate {@code property}
     * @return <b>this</b> instance to allow cascading calls
     * @since 1.4.0
     */
    default OntIndividual removeNegativeAssertion(OntProperty property, RDFNode value) {
        negativeAssertions(property)
                .filter(x -> value == null || value.equals(x.getTarget()))
                .collect(Collectors.toList())
                .forEach(x -> getModel().removeOntObject(x));
        return this;
    }

    /**
     * Removes a different individual statement for this and specified individuals,
     * including the statement's annotation.
     * No-op in case no different individuals are found.
     * Removes all triples with the predicate {@code owl:differentFrom} if {@code null} is specified.
     *
     * @param other {@link Resource} or {@code null} to remove all different individuals
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDifferentFromStatement(OntIndividual)
     * @see #addDifferentIndividual(OntIndividual)
     * @see OntDisjoint.Individuals
     * @since 1.4.0
     */
    default OntIndividual removeDifferentIndividual(Resource other) {
        remove(OWL.differentFrom, other);
        return this;
    }

    /**
     * Removes a same individual statement for this and specified individuals,
     * including the statement's annotation.
     * No-op in case no same individuals are found.
     * Removes all triples with the predicate {@code owl:sameAs} if {@code null} is specified.
     *
     * @param other {@link Resource} or {@code null} to remove all same individuals
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSameAsStatement(OntIndividual)
     * @see #addSameIndividual(OntIndividual)
     * @since 1.4.0
     */
    default OntIndividual removeSameIndividual(Resource other) {
        remove(OWL.sameAs, other);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addComment(String txt) {
        return addComment(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addComment(String txt, String lang) {
        return annotate(getModel().getRDFSComment(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addLabel(String txt) {
        return addLabel(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual addLabel(String txt, String lang) {
        return annotate(getModel().getRDFSLabel(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual annotate(OntNAP predicate, String txt, String lang) {
        return annotate(predicate, getModel().createLiteral(txt, lang));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntIndividual annotate(OntNAP predicate, RDFNode value) {
        addAnnotation(predicate, value);
        return this;
    }

    /**
     * An interface for <b>Named</b> Individual which is an {@link OWL Entity OntEntity}.
     * <p>
     * Created by szuev on 01.11.2016.
     */
    interface Named extends OntIndividual, OntEntity {
    }

    /**
     * An interface for Anonymous Individuals.
     * The anonymous individual is a blank node ({@code _:a}) which satisfies one of the following conditions:
     * <ul>
     * <li>it has a class declaration (i.e. there is a triple {@code _:a rdf:type C},
     * where {@code C} is a {@link OntCE class expression})</li>
     * <li>it is a subject or an object in a statement with predicate
     * {@link OWL#sameAs owl:sameAs} or {@link OWL#differentFrom owl:differentFrom}</li>
     * <li>it is contained in a {@code rdf:List} with predicate {@code owl:distinctMembers} or {@code owl:members}
     * in a blank node with {@code rdf:type = owl:AllDifferent}, see {@link OntDisjoint.Individuals}</li>
     * <li>it is contained in a {@code rdf:List} with predicate {@code owl:oneOf}
     * in a blank node with {@code rdf:type = owl:Class}, see {@link OntCE.OneOf}</li>
     * <li>it is a part of {@link OntNPA owl:NegativePropertyAssertion} section with predicates
     * {@link OWL#sourceIndividual owl:sourceIndividual} or {@link OWL#targetIndividual owl:targetIndividual}</li>
     * <li>it is an object with predicate {@code owl:hasValue} inside {@code _:x rdf:type owl:Restriction}
     * (see {@link OntCE.ObjectHasValue Object Property HasValue Restriction})</li>
     * <li>it is a subject or an object in a statement where predicate is
     * an uri-resource with {@code rdf:type = owl:AnnotationProperty}
     * (i.e. {@link OntNAP annotation property} assertion {@code s A t})</li>
     * <li>it is a subject in a triple which corresponds data property assertion {@code _:a R v}
     * (where {@code R} is a {@link OntNDP datatype property}, {@code v} is a {@link Literal literal})</li>
     * <li>it is a subject or an object in a triple which corresponds object property assertion {@code _:a1 PN _:a2}
     * (where {@code PN} is a {@link OntNOP named object property}, and {@code _:ai} are individuals)</li>
     * </ul>
     * <p>
     * Created by szuev on 10.11.2016.
     */
    interface Anonymous extends OntIndividual {

        /**
         * {@inheritDoc}
         * For an anonymous individual a primary class assertion is also a definition, so its deletion is prohibited.
         *
         * @param clazz {@link OntCE}, not {@code null}
         * @return <b>this</b> instance to allow cascading calls
         * @throws OntJenaException in case the individual has only one class assertion and it is for the given class
         */
        @Override
        Anonymous detachClass(Resource clazz) throws OntJenaException;
    }

}
