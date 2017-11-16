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
package org.codice.alliance.test.itests;

import static com.jayway.restassured.RestAssured.when;
import static org.codice.ddf.itests.common.AbstractIntegrationTest.DynamicUrl.INSECURE_ROOT;
import static org.codice.ddf.itests.common.csw.CswTestCommons.GMD_CSW_FEDERATED_SOURCE_FACTORY_PID;
import static org.codice.ddf.itests.common.csw.CswTestCommons.getCswSourceProperties;
import static org.hamcrest.Matchers.hasXPath;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import ddf.catalog.data.types.Core;
import java.io.IOException;
import java.util.Map;
import org.codice.alliance.test.itests.common.AbstractAllianceIntegrationTest;
import org.codice.ddf.itests.common.annotations.AfterExam;
import org.codice.ddf.itests.common.annotations.BeforeExam;
import org.codice.ddf.itests.common.csw.mock.FederatedCswMockServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

/** Alliance MGMP Application integration tests. */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class MgmpTest extends AbstractAllianceIntegrationTest {

  private static final String MGMP_SOURCE_ID = "mgmpSource";

  private FederatedCswMockServer mockMgmpServer;

  private static final String CSW_STUB_SOURCE_ID = "cswStubServer";

  private static final DynamicPort CSW_STUB_SERVER_PORT =
      new DynamicPort("org.codice.alliance.csw_stub_server_port", 9);

  private static final DynamicUrl CSW_STUB_SERVER_URL =
      new DynamicUrl(INSECURE_ROOT, CSW_STUB_SERVER_PORT, "/services/csw");

  private static final String RECORD_TITLE_1 = "myTitle";

  private static final String MGMP_METACARD_TYPE = "gmdMetacardType";

  @BeforeExam
  public void beforeAllianceTest() throws Exception {
    try {
      waitForSystemReady();
      startMockResources();

      configureMgmpSource();
      getCatalogBundle().waitForFederatedSource(MGMP_SOURCE_ID);

    } catch (Exception e) {
      LOGGER.error("Failed in @BeforeExam: ", e);
      fail("Failed in @BeforeExam: " + e.getMessage());
    }
  }

  private void configureMgmpSource() throws IOException {
    Map<String, Object> mgmpProperties =
        getCswSourceProperties(
            MGMP_SOURCE_ID,
            GMD_CSW_FEDERATED_SOURCE_FACTORY_PID,
            CSW_STUB_SERVER_URL.getUrl(),
            getServiceManager());

    getServiceManager().createManagedService(GMD_CSW_FEDERATED_SOURCE_FACTORY_PID, mgmpProperties);
  }

  private void startMockResources() throws Exception {
    mockMgmpServer =
        new FederatedCswMockServer(
            CSW_STUB_SOURCE_ID, INSECURE_ROOT, Integer.parseInt(CSW_STUB_SERVER_PORT.getPort()));
    mockMgmpServer.setupDefaultCapabilityResponseExpectation(
        getAllianceItestResource("mgmp-mock-capabilities-response.xml"));
    mockMgmpServer.setupDefaultQueryResponseExpectation(
        getAllianceItestResource("mgmp-mock-query-response.xml"));
    mockMgmpServer.start();
  }

  @Test
  public void testMgmpCswOpenSearchGetAll() throws Exception {
    String queryUrl = OPENSEARCH_PATH.getUrl() + "?q=*&format=xml&src=" + MGMP_SOURCE_ID;

    // @formatter:off
    when()
        .get(queryUrl)
        .then()
        .log()
        .all()
        .assertThat()
        .body(
            hasXPath(
                "/metacards/metacard/string[@name='"
                    + Core.TITLE
                    + "']/value[text()='"
                    + RECORD_TITLE_1
                    + "']"),
            hasXPath("/metacards/metacard/geometry/value"),
            hasXPath("/metacards/metacard/stringxml"),
            /* Assert that the MGMP transformer takes precedence over the GMD transformer */

            hasXPath("/metacards/metacard/type", is(MGMP_METACARD_TYPE)));
    // @formatter:on
  }

  @AfterExam
  public void afterAllianceTest() throws Exception {
    if (mockMgmpServer != null) {
      mockMgmpServer.stop();
    }
  }
}
