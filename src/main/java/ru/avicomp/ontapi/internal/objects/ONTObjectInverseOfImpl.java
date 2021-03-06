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

import org.apache.jena.graph.BlankNodeId;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import ru.avicomp.ontapi.internal.InternalObjectFactory;
import ru.avicomp.ontapi.internal.ONTObject;
import ru.avicomp.ontapi.jena.model.OntGraphModel;
import ru.avicomp.ontapi.jena.model.OntNOP;
import ru.avicomp.ontapi.jena.model.OntOPE;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An {@link OWLObjectInverseOf} implementation that is also {@link ONTObject}.
 * Created by @ssz on 19.08.2019.
 *
 * @see ru.avicomp.ontapi.owlapi.objects.OWLObjectInverseOfImpl
 * @see OntOPE.Inverse
 * @since 1.4.3
 */
@SuppressWarnings("WeakerAccess")
public class ONTObjectInverseOfImpl
        extends ONTExpressionImpl<OntOPE.Inverse> implements OWLObjectInverseOf, ModelObject<OWLObjectInverseOf> {

    public ONTObjectInverseOfImpl(BlankNodeId n, Supplier<OntGraphModel> m) {
        super(n, m);
    }

    /**
     * Wraps the given {@link OntOPE.Inverse} as {@link OWLObjectInverseOf} and {@link ONTObject}.
     *
     * @param iop   {@link OntOPE.Inverse}, not {@code null}
     * @param factory {@link InternalObjectFactory}, not {@code null}
     * @param model a provider of non-null {@link OntGraphModel}, cannot be {@code null}
     * @return {@link ONTObjectInverseOfImpl}
     */
    public static ONTObjectInverseOfImpl create(OntOPE.Inverse iop,
                                                InternalObjectFactory factory,
                                                Supplier<OntGraphModel> model) {
        ONTObjectInverseOfImpl res = new ONTObjectInverseOfImpl(iop.asNode().getBlankNodeId(), model);
        res.putContent(res.initContent(iop, factory));
        return res;
    }

    @Override
    public OntOPE.Inverse asRDFNode() {
        return as(OntOPE.Inverse.class);
    }

    @Override
    public OWLObjectInverseOf getOWLObject() {
        return this;
    }

    @Override
    protected Object[] collectContent(OntOPE.Inverse pe, InternalObjectFactory factory) {
        return new Object[]{pe.getDirect().getURI()};
    }

    @Override
    protected Object[] initContent(OntOPE.Inverse pe, InternalObjectFactory factory) {
        OntNOP p = pe.getDirect();
        this.hashCode = OWLObject.hashIteration(hashIndex(), factory.getProperty(p).hashCode());
        return new Object[]{p.getURI()};
    }

    @Override
    public Stream<ONTObject<? extends OWLObject>> objects() {
        return Stream.of(getONTObjectProperty());
    }

    public ONTObject<OWLObjectProperty> getONTObjectProperty() {
        return findONTObjectProperty(getObjectFactory());
    }

    public ONTObject<OWLObjectProperty> findONTObjectProperty(InternalObjectFactory factory) {
        return ONTObjectPropertyImpl.find((String) getContent()[0], factory, model);
    }

    @Override
    public OWLObjectProperty getInverse() {
        return getNamedProperty();
    }

    @Override
    public OWLObjectProperty getNamedProperty() {
        return getONTObjectProperty().getOWLObject();
    }

    @Override
    public OWLObjectInverseOf eraseModel() {
        return getDataFactory().getOWLObjectInverseOf(eraseModel(getNamedProperty()));
    }

    @Override
    public Set<OWLEntity> getSignatureSet() {
        return createSet(getNamedProperty());
    }

    @Override
    public boolean containsObjectProperty(OWLObjectProperty property) {
        return getNamedProperty().equals(property);
    }

    @Override
    public boolean canContainNamedClasses() {
        return false;
    }

    @Override
    public boolean canContainDatatypes() {
        return false;
    }

    @Override
    public boolean canContainNamedIndividuals() {
        return false;
    }

    @Override
    public boolean canContainDataProperties() {
        return false;
    }

    @Override
    public Set<OWLObjectProperty> getObjectPropertySet() {
        return createSet(getNamedProperty());
    }

    @Override
    public boolean canContainAnnotationProperties() {
        return false;
    }

    @Override
    public boolean canContainClassExpressions() {
        return false;
    }

    @Override
    public boolean canContainAnonymousIndividuals() {
        return false;
    }
}
