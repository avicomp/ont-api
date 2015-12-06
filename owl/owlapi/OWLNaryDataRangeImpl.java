/* This file is part of the OWL API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright 2014, The University of Manchester
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. */
package uk.ac.manchester.cs.owl.owlapi;

import static org.semanticweb.owlapi.util.CollectionFactory.sortOptionally;
import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLNaryDataRange;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * @author Matthew Horridge, The University of Manchester, Information
 *         Management Group
 * @since 3.0.0
 */
public abstract class OWLNaryDataRangeImpl extends OWLObjectImpl implements OWLNaryDataRange {

    private final @Nonnull List<? extends OWLDataRange> operands;

    protected OWLNaryDataRangeImpl(Collection<? extends OWLDataRange> operands) {
        Collection<? extends OWLDataRange> ops = checkNotNull(operands, "operands cannot be null");
        this.operands = sortOptionally(ops.stream().distinct());
    }

    protected OWLNaryDataRangeImpl(Stream<? extends OWLDataRange> operands) {
        Stream<? extends OWLDataRange> ops = checkNotNull(operands, "operands cannot be null");
        this.operands = asList(ops.distinct().sorted());
    }

    @Override
    public Stream<? extends OWLDataRange> operands() {
        return operands.stream();
    }

    @Override
    protected int compareObjectOfSameType(OWLObject object) {
        return compareStreams(operands(), ((OWLNaryDataRange) object).operands());
    }
}
