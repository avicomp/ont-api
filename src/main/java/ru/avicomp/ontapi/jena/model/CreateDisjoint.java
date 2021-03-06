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

import java.util.Arrays;
import java.util.Collection;

/**
 * A technical interface to generate {@link OntDisjoint Disjoint Resource}s.
 * Created by @szz on 14.05.2019.
 *
 * @since 1.4.0
 */
interface CreateDisjoint {

    /**
     * Creates a Disjoint Classes Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDisjointClasses .
     * _:x owl:members ( C1 ... Cn ) .
     * }</pre>
     *
     * @param classes {@code Collection} of {@link OntCE Class Expression}s without {@code null}-elements
     * @return {@link OntDisjoint.Classes}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Disjoint_Classes'>9.1.3 Disjoint Classes</a>
     */
    OntDisjoint.Classes createDisjointClasses(Collection<OntCE> classes);

    /**
     * Creates a Different Individuals Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDifferent .
     * _:x owl:members ( a1 ... an ).
     * }</pre>
     * Note: instead of {@link ru.avicomp.ontapi.jena.vocabulary.OWL#members owl:members}, alternatively,
     * the predicate {@link ru.avicomp.ontapi.jena.vocabulary.OWL#distinctMembers owl:distinctMembers} can be used.
     *
     * @param individuals {@code Collection} of {@link OntIndividual Individual}s without {@code null}-elements
     * @return {@link OntDisjoint.Individuals}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Individual_Inequality'>9.6.2 Individual Inequality </a>
     */
    OntDisjoint.Individuals createDifferentIndividuals(Collection<OntIndividual> individuals);

    /**
     * Creates a Disjoint Object Properties Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDisjointProperties .
     * _:x owl:members ( P1 ... Pn ) .
     * }</pre>
     *
     * @param properties {@code Collection} of {@link OntOPE object property expression}s without {@code null}-elements
     * @return {@link OntDisjoint.ObjectProperties}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Disjoint_Object_Properties'>9.2.3 Disjoint Object Properties</a>
     */
    OntDisjoint.ObjectProperties createDisjointObjectProperties(Collection<OntOPE> properties);

    /**
     * Creates a Disjoint Data Properties Axiom Resource.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:AllDisjointProperties .
     * _:x owl:members ( R1 ... Rn ) .
     * }</pre>
     *
     * @param properties {@code Collection} of {@link OntNDP data properties} without {@code null}-elements
     * @return {@link OntDisjoint.DataProperties}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Disjoint_Data_Properties'>9.3.3 Disjoint Data Properties</a>
     */
    OntDisjoint.DataProperties createDisjointDataProperties(Collection<OntNDP> properties);

    /**
     * Creates a Disjoint Classes Axiom Resource.
     *
     * @param classes Array of {@link OntCE Class Expression}s without {@code null}-elements
     * @return {@link OntDisjoint.Classes}
     * @see #createDisjointClasses(Collection)
     * @since 1.4.0
     */
    default OntDisjoint.Classes createDisjointClasses(OntCE... classes) {
        return createDisjointClasses(Arrays.asList(classes));
    }

    /**
     * Creates a Different Individuals Axiom Resource.
     *
     * @param individuals Array of {@link OntIndividual individual}s without {@code null}-elements
     * @return {@link OntDisjoint.Individuals}
     * @see #createDifferentIndividuals(Collection)
     * @since 1.4.0
     */
    default OntDisjoint.Individuals createDifferentIndividuals(OntIndividual... individuals) {
        return createDifferentIndividuals(Arrays.asList(individuals));
    }

    /**
     * Creates a Disjoint Object Properties Axiom Resource.
     *
     * @param properties Array of {@link OntOPE Object Property Expression}s without {@code null}-elements
     * @return {@link OntDisjoint.ObjectProperties}
     * @see #createDisjointObjectProperties(Collection)
     * @since 1.4.0
     */
    default OntDisjoint.ObjectProperties createDisjointObjectProperties(OntOPE... properties) {
        return createDisjointObjectProperties(Arrays.asList(properties));
    }

    /**
     * Creates a Disjoint Data Properties Axiom Resource.
     *
     * @param properties Array of {@link OntNDP Data Properties} without {@code null}-elements
     * @return {@link OntDisjoint.DataProperties}
     * @see #createDisjointDataProperties(Collection)
     * @since 1.4.0
     */
    default OntDisjoint.DataProperties createDisjointDataProperties(OntNDP... properties) {
        return createDisjointDataProperties(Arrays.asList(properties));
    }
}
