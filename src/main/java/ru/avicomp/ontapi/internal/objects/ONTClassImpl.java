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

package ru.avicomp.ontapi.internal.objects;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import ru.avicomp.ontapi.OntApiException;
import ru.avicomp.ontapi.internal.InternalObjectFactory;
import ru.avicomp.ontapi.internal.ModelObjectFactory;
import ru.avicomp.ontapi.internal.ONTObject;
import ru.avicomp.ontapi.jena.model.OntClass;
import ru.avicomp.ontapi.jena.model.OntGraphModel;
import ru.avicomp.ontapi.jena.vocabulary.OWL;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An {@link OWLClass} implementation that is also an instance of {@link ONTObject}.
 * Created by @ssz on 07.08.2019.
 *
 * @see ru.avicomp.ontapi.owlapi.objects.entity.OWLClassImpl
 * @see ONTAnonymousClassExpressionImpl
 * @since 1.4.3
 */
public class ONTClassImpl extends ONTEntityImpl<OWLClass> implements OWLClass {

    public ONTClassImpl(String uri, Supplier<OntGraphModel> m) {
        super(uri, m);
    }

    /**
     * Using the {@code factory} finds or creates an {@link OWLClass} instance.
     *
     * @param uri     {@code String}, not {@code null}
     * @param factory {@link InternalObjectFactory}, not {@code null}
     * @param model   a {@code Supplier} with a {@link OntGraphModel},
     *                which is only used in case the {@code factory} has no reference to a model
     * @return an {@link ONTObject} which is {@link OWLClass}
     */
    public static ONTObject<OWLClass> find(String uri,
                                           InternalObjectFactory factory,
                                           Supplier<OntGraphModel> model) {
        if (factory instanceof ModelObjectFactory) {
            return ((ModelObjectFactory) factory).getClass(uri);
        }
        return factory.getClass(OntApiException.mustNotBeNull(model.get().getOntClass(uri)));
    }

    @Override
    public OntClass asRDFNode() {
        return as(OntClass.class);
    }

    @Override
    public boolean isOWLThing() {
        return equals(OWL.Thing);
    }

    @Override
    public boolean isOWLNothing() {
        return equals(OWL.Nothing);
    }

    @Override
    public boolean isNamedClass() {
        return true;
    }

    @Override
    public boolean isClassExpression() {
        return true;
    }

    @Override
    public Set<OWLClass> getNamedClassSet() {
        return createSet(this);
    }

    @Override
    public Set<OWLClassExpression> getClassExpressionSet() {
        return createSet(this);
    }

    @FactoryAccessor
    @Override
    public OWLClassExpression getNNF() {
        return eraseModel();
    }

    @FactoryAccessor
    @Override
    public OWLClassExpression getComplementNNF() {
        return getObjectComplementOf();
    }

    @FactoryAccessor
    @Override
    public OWLObjectComplementOf getObjectComplementOf() {
        return getDataFactory().getOWLObjectComplementOf(eraseModel());
    }

    @Override
    public Set<OWLClassExpression> asConjunctSet() {
        return createSet(this);
    }

    @Override
    public Stream<OWLClassExpression> conjunctSet() {
        return Stream.of(this);
    }

    @Override
    public boolean containsConjunct(@Nullable OWLClassExpression ce) {
        return equals(ce);
    }

    @Override
    public Set<OWLClassExpression> asDisjunctSet() {
        return createSet(this);
    }

    @Override
    public Stream<OWLClassExpression> disjunctSet() {
        return Stream.of(this);
    }

}
