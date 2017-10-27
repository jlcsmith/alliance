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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.hamcrest.Matchers;
import org.junit.Test;

public class MgmpUtilityTest {

  private static final BidiMap SECURITY_MAP;

  public static final List<String> SECURITY_LIST =
      Arrays.asList("secret=SECRET", "official=OFFICIAL", "officialSensitive=OFFICIAL-SENSITIVE");

  static {
    SECURITY_MAP = new TreeBidiMap();
    SECURITY_MAP.put("secret", "SECRET");
    SECURITY_MAP.put("official", "OFFICIAL");
    SECURITY_MAP.put("officialSensitive", "OFFICIAL-SENSITIVE");
  }

  @Test
  public void testSecurityMappingListToMapBadAndNullValues() {
    List<String> list = new ArrayList<>();
    list.add("officialSensitive=");
    list.add("");
    list.add(null);
    BidiMap treeBidiMap = MgmpUtility.securityMappingListToMap(list);
    assertThat(treeBidiMap, notNullValue());
    assertThat(treeBidiMap.size(), is(0));
  }

  @Test
  public void testSecurityMappingListToMap() {
    BidiMap bidiMap = MgmpUtility.securityMappingListToMap(SECURITY_LIST);
    assertThat(bidiMap, Matchers.notNullValue());
    assertThat(bidiMap.size(), is(3));
    assertThat(bidiMap.get("secret"), is("SECRET"));
    assertThat(bidiMap.get("official"), is("OFFICIAL"));
    assertThat(bidiMap.get("officialSensitive"), is("OFFICIAL-SENSITIVE"));
  }

  @Test
  public void testSecurityMappingToList() {
    List<String> list = MgmpUtility.securityMapToList(SECURITY_MAP);
    assertThat(list, hasSize(3));
    assertThat(list, Matchers.hasItem("secret=SECRET"));
    assertThat(list, Matchers.hasItem("official=OFFICIAL"));
    assertThat(list, Matchers.hasItem("officialSensitive=OFFICIAL-SENSITIVE"));
  }
}
