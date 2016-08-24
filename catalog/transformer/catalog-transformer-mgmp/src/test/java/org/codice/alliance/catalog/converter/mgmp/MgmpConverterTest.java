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
package org.codice.alliance.catalog.converter.mgmp;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.AssociationsAttributes;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.DateTimeAttributes;
import ddf.catalog.data.impl.types.LocationAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.impl.types.ValidationAttributes;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.Topic;

public class MgmpConverterTest {

    private MetacardImpl metacard;

    private MgmpConverter mgmpConverter;

    @Before
    public void setup() {
        MetacardType metacardType = new MetacardTypeImpl("foo",
                Arrays.asList(new AssociationsAttributes(),
                        new ContactAttributes(),
                        new MediaAttributes(),
                        new DateTimeAttributes(),
                        new LocationAttributes(),
                        new ValidationAttributes(),
                        new IsrAttributes()));
        metacard = new MetacardImpl(metacardType);

        metacard.setId("22407eee80044d92afedcac07fff661b");
        metacard.setAttribute(new AttributeImpl(Isr.COMMENTS,
                Arrays.asList("comment1", "comment2")));
        metacard.setAttribute(new AttributeImpl(Isr.CATEGORY,
                Arrays.asList("category1", "category2")));
        metacard.setAttribute(new AttributeImpl(Core.LANGUAGE, Arrays.asList("eng", "fr", "de")));
        metacard.setLocation("POLYGON (( 0 0, 10 0, 10 10, 0 10, 0 0 ))");
        metacard.setAttribute(Location.COUNTRY_CODE, (Serializable) Arrays.asList("USA", "GBR"));
        metacard.setAttribute(GmdConstants.RESOURCE_STATUS, "status1");
        Date start1 = new Date();
        Date end1 = new Date(start1.getTime() + 10000);
        Date start2 = new Date(start1.getTime() + 50000);
        metacard.setAttribute(DateTime.START, (Serializable) Arrays.asList(start1, start2));
        metacard.setAttribute(DateTime.END, (Serializable) Arrays.asList(end1, start2));
        metacard.setAttribute(Location.ALTITUDE, (Serializable) Arrays.asList(1D, 2D, 3D, 4D, 5D));
        metacard.setModifiedDate(new Date());
        metacard.setCreatedDate(new Date());
        metacard.setExpirationDate(new Date());
        Date metacardModified = new Date();
        metacard.setAttribute(Core.METACARD_MODIFIED, metacardModified);
        metacard.setAttribute(Core.METACARD_CREATED, new Date(metacardModified.getTime() + 10000));
        metacard.setAttribute(Contact.PUBLISHER_NAME, (Serializable) Arrays.asList("pub1", "pub2"));
        metacard.setAttribute(Contact.PUBLISHER_PHONE,
                (Serializable) Arrays.asList("phone1", "phone2"));
        metacard.setAttribute(Contact.PUBLISHER_EMAIL,
                (Serializable) Arrays.asList("email1", "email2"));
        metacard.setAttribute(Contact.PUBLISHER_ADDRESS,
                (Serializable) Arrays.asList("addr1", "addr2"));
        metacard.setResourceURI(URI.create("http://127.0.0.1/foo/bar/index.html"));
        metacard.setAttribute(Associations.RELATED, "22407eee80044d92afedcac07fff661c");
        metacard.setAttribute(new AttributeImpl(Topic.CATEGORY,
                Arrays.asList("farming", "biota", "boundaries")));
        metacard.setAttribute(new AttributeImpl(Topic.KEYWORD,
                Arrays.asList("keyword1", "keyword2", "keyword3")));
        metacard.setAttribute(Media.FORMAT, "format");
        metacard.setAttribute(Media.FORMAT_VERSION, "format-version");
        metacard.setAttribute(Core.DESCRIPTION, "this is the description string");
        metacard.setTitle("theTitle");
        metacard.setContentTypeName("text/plain");
        metacard.setAttribute(MgmpConverter.SECURITY_METADATA_DISSEMINATION_ATTRIBUTE,
                "Releasable to");
        metacard.setAttribute(MgmpConverter.SECURITY_METADATA_RELEASABILITY_ATTRIBUTE,
                (Serializable) Arrays.asList("USA", "AUS"));
        metacard.setAttribute(MgmpConverter.SECURITY_RESOURCE_DISSEMINATION, "Releasable to");
        metacard.setAttribute(MgmpConverter.SECURITY_RESOURCE_RELEASABILITY,
                (Serializable) Arrays.asList("USA", "AUS"));
        metacard.setAttribute(MgmpConverter.SECURITY_RESOURCE_CLASSIFICATION_ATTRIBUTE, "secret");
        metacard.setAttribute(MgmpConverter.SECURITY_METADATA_CLASSIFICATION_ATTRIBUTE, "secret");

        metacard.setAttribute(MgmpConverter.SECURITY_METADATA_ORIGINATOR_CLASSIFICATION,
                "security-metadata-originator-classification");
        metacard.setAttribute(Location.COORDINATE_REFERENCE_SYSTEM_CODE,
                (Serializable) Arrays.asList("MGMP:001", "MGMP:002"));
        metacard.setAttribute(Isr.CLOUD_COVER, 30);
        metacard.setAttribute(MgmpConverter.SECURITY_METADATA_ORIGINATOR_CLASSIFICATION,
                "security-metadata-originator-classification");
        metacard.setAttribute(MgmpConverter.SECURITY_RESOURCE_ORIGINATOR_CLASSIFICATION,
                "security-resource-originator-classification");
        metacard.setAttribute(Contact.POINT_OF_CONTACT_NAME, "point-of-contact-name-1");
        metacard.setAttribute(Contact.POINT_OF_CONTACT_ADDRESS,
                (Serializable) Arrays.asList("point-of-contact-addr-1", "point-of-contact-addr-2"));
        metacard.setAttribute(Contact.POINT_OF_CONTACT_PHONE,
                (Serializable) Arrays.asList("point-of-contact-phone-1",
                        "point-of-contact-phone-2"));
        metacard.setAttribute(Contact.POINT_OF_CONTACT_EMAIL,
                (Serializable) Arrays.asList("point-of-contact-email-1",
                        "point-of-contact-email-2"));
        metacard.setAttribute(MgmpConverter.RESOURCE_RATING, 2D);
        metacard.setAttribute(MgmpConverter.RESOURCE_RATING_SYSTEM, "NIIRS");

        mgmpConverter = new MgmpConverter();
    }

    @Test
    public void testSchemaCompliance() throws IOException, SAXException {

        StringWriter stringWriter = new StringWriter();
        PrettyPrintWriter writer = new PrettyPrintWriter(stringWriter, new NoNameCoder());
        TreeMarshaller context = new TreeMarshaller(writer, null, null);

        mgmpConverter.marshal(metacard, writer, context);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(new LSResourceResolver() {

            private Map<Pair<String, String>, String> schemaLocations;

            private Map<Pair<String, String>, LSInput> inputs;

            {
                inputs = new HashMap<>();

                schemaLocations = new HashMap<>();

                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "gmd.xsd"), "/schemas/iso/19139/20070417/gmd/gmd.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "metadataEntity.xsd"), "/schemas/iso/19139/20070417/gmd/metadataEntity.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                                "metadataApplication.xsd"),
                        "/schemas/iso/19139/20070417/gmd/metadataApplication.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                                "spatialRepresentation.xsd"),
                        "/schemas/iso/19139/20070417/gmd/spatialRepresentation.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "citation.xsd"), "/schemas/iso/19139/20070417/gmd/citation.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "referenceSystem.xsd"), "/schemas/iso/19139/20070417/gmd/referenceSystem.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "extent.xsd"), "/schemas/iso/19139/20070417/gmd/extent.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "metadataExtension.xsd"), "/schemas/iso/19139/20070417/gmd/metadataExtension.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "content.xsd"), "/schemas/iso/19139/20070417/gmd/content.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "applicationSchema.xsd"), "/schemas/iso/19139/20070417/gmd/applicationSchema.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                                "portrayalCatalogue.xsd"),
                        "/schemas/iso/19139/20070417/gmd/portrayalCatalogue.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "dataQuality.xsd"), "/schemas/iso/19139/20070417/gmd/dataQuality.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "identification.xsd"), "/schemas/iso/19139/20070417/gmd/identification.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "constraints.xsd"), "/schemas/iso/19139/20070417/gmd/constraints.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "distribution.xsd"), "/schemas/iso/19139/20070417/gmd/distribution.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "maintenance.xsd"), "/schemas/iso/19139/20070417/gmd/maintenance.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gmd",
                        "freeText.xsd"), "/schemas/iso/19139/20070417/gmd/freeText.xsd");

                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gco",
                        "gco.xsd"), "/schemas/iso/19139/20070417/gco/gco.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gco",
                        "gcoBase.xsd"), "/schemas/iso/19139/20070417/gco/gcoBase.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gco",
                        "basicTypes.xsd"), "/schemas/iso/19139/20070417/gco/basicTypes.xsd");

                schemaLocations.put(new ImmutablePair<>("http://www.w3.org/1999/xlink",
                        "xlink.xsd"), "/schemas/xlink/xlink.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.w3.org/XML/1998/namespace",
                        "xml.xsd"), "/schemas/xml/2001/xml.xsd");

                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "feature.xsd"), "/schemas/gml/3.2.1/feature.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "geometryAggregates.xsd"), "/schemas/gml/3.2.1/geometryAggregates.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "geometryPrimitives.xsd"), "/schemas/gml/3.2.1/geometryPrimitives.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "geometryBasic2d.xsd"), "/schemas/gml/3.2.1/geometryBasic2d.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "geometryBasic0d1d.xsd"), "/schemas/gml/3.2.1/geometryBasic0d1d.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "measures.xsd"), "/schemas/gml/3.2.1/measures.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "units.xsd"), "/schemas/gml/3.2.1/units.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "dictionary.xsd"), "/schemas/gml/3.2.1/dictionary.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "gmlBase.xsd"), "/schemas/gml/3.2.1/gmlBase.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "gml.xsd"), "/schemas/gml/3.2.1/gml.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "dynamicFeature.xsd"), "/schemas/gml/3.2.1/dynamicFeature.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "temporal.xsd"), "/schemas/gml/3.2.1/temporal.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "direction.xsd"), "/schemas/gml/3.2.1/direction.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "topology.xsd"), "/schemas/gml/3.2.1/topology.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "geometryComplexes.xsd"), "/schemas/gml/3.2.1/geometryComplexes.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "coverage.xsd"), "/schemas/gml/3.2.1/coverage.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "valueObjects.xsd"), "/schemas/gml/3.2.1/valueObjects.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "grids.xsd"), "/schemas/gml/3.2.1/grids.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                                "coordinateReferenceSystems.xsd"),
                        "/schemas/gml/3.2.1/coordinateReferenceSystems.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "coordinateSystems.xsd"), "/schemas/gml/3.2.1/coordinateSystems.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "referenceSystems.xsd"), "/schemas/gml/3.2.1/referenceSystems.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "datums.xsd"), "/schemas/gml/3.2.1/datums.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "coordinateOperations.xsd"), "/schemas/gml/3.2.1/coordinateOperations.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "observation.xsd"), "/schemas/gml/3.2.1/observation.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "temporalReferenceSystems.xsd"), "/schemas/gml/3.2.1/temporalReferenceSystems.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "temporalTopology.xsd"), "/schemas/gml/3.2.1/temporalTopology.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "deprecatedTypes.xsd"), "/schemas/gml/3.2.1/deprecatedTypes.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.opengis.net/gml/3.2",
                        "basicTypes.xsd"), "/schemas/gml/3.2.1/basicTypes.xsd");

                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gss",
                        "gss.xsd"), "/schemas/iso/19139/20070417/gss/gss.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gss",
                        "geometry.xsd"), "/schemas/iso/19139/20070417/gss/geometry.xsd");

                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gts",
                        "gts.xsd"), "/schemas/iso/19139/20070417/gts/gts.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gts",
                        "temporalObjects.xsd"), "/schemas/iso/19139/20070417/gts/temporalObjects.xsd");

                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gsr",
                        "gsr.xsd"), "/schemas/iso/19139/20070417/gsr/gsr.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/gsr",
                                "spatialReferencing.xsd"),
                        "/schemas/iso/19139/20070417/gsr/spatialReferencing.xsd");

                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/srv",
                        "srv.xsd"), "/schemas/iso/19139/20060504/srv/srv.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/srv",
                        "serviceMetadata.xsd"), "/schemas/iso/19139/20060504/srv/serviceMetadata.xsd");
                schemaLocations.put(new ImmutablePair<>("http://www.isotc211.org/2005/srv",
                        "serviceModel.xsd"), "/schemas/iso/19139/20060504/srv/serviceModel.xsd");

            }

            @Override
            public LSInput resolveResource(String type, String namespaceURI, String publicId,
                    String systemId, String baseURI) {

                String fileName = new java.io.File(systemId).getName();

                Pair<String, String> key = new ImmutablePair<>(namespaceURI, fileName);

                if (inputs.containsKey(key)) {
                    return inputs.get(key);
                }

                LSInput input = new DOMInputImpl();

                InputStream is = getClass().getResourceAsStream(schemaLocations.get(key));
                input.setByteStream(is);
                input.setBaseURI(baseURI);
                input.setSystemId(systemId);
                inputs.put(key, input);
                return input;
            }
        });

        Source mgmpSchemaSource = new StreamSource(getClass().getResourceAsStream("/schemas/mgmp.xsd"));

        Schema schema = schemaFactory.newSchema(new Source[] {mgmpSchemaSource});

        try {
            schema.newValidator()
                    .validate(new StreamSource(new StringReader(stringWriter.toString())));
        } catch (SAXException | IOException e) {
            fail("Generated MGMPv2 Response does not conform to Schema" + e.getMessage());
        }

    }

}
