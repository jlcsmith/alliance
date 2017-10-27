/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.catalog.mgmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MgmpUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(MgmpUtility.class);

  private MgmpUtility() {}

  public static TreeBidiMap securityMappingListToMap(List<String> securityMappingList) {
    TreeBidiMap securityMap = new TreeBidiMap();
    securityMappingList.forEach(
        string -> {
          if (StringUtils.isNotEmpty(string)) {
            String[] mappingArray = string.split("=");

            if (mappingArray.length == 2) {
              LOGGER.debug("Adding mapping : {} = {}", mappingArray[0], mappingArray[1]);
              securityMap.put(mappingArray[0], mappingArray[1]);
            } else {
              LOGGER.debug("Unable to split string {}", string);
            }
          }
        });
    return securityMap;
  }

  public static List<String> securityMapToList(BidiMap securityMap) {
    List<String> stringList = new ArrayList<>();
    for (Object entry : securityMap.entrySet()) {
      Map.Entry<String, String> temp = (Map.Entry<String, String>) entry;
      stringList.add(temp.getKey() + "=" + temp.getValue());
    }
    return stringList;
  }
}
