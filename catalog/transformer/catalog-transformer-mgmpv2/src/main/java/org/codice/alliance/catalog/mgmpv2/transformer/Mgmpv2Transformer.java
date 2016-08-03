/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 **/
package org.codice.alliance.catalog.mgmpv2.transformer;

import org.codice.ddf.spatial.ogc.csw.catalog.transformer.GmdTransformer;

import ddf.catalog.data.MetacardType;

public class Mgmpv2Transformer extends GmdTransformer {

    private MetacardType mgmpv2MetacardType;

    public Mgmpv2Transformer(MetacardType metacardType) {
        super(metacardType);
        this.mgmpv2MetacardType = metacardType;
    }

    @Override
    public String toString() {
        return "Mgmpv2Transformer{" +
                "mgmpv2MetacardType=" + mgmpv2MetacardType +
                '}';
    }
}
