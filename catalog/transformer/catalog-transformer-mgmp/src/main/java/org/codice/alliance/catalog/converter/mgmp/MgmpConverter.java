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

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.catalog.transformer.mgmp.MgmpConstants;
import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;
import org.codice.ddf.spatial.ogc.csw.catalog.converter.AbstractGmdConverter;
import org.codice.ddf.spatial.ogc.csw.catalog.converter.XstreamPathValueTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.io.path.Path;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.data.types.Topic;

/**
 * Not thread safe.
 */
public class MgmpConverter extends AbstractGmdConverter {

    // TODO i don't know how to output ISR_DATA_QUALITY, maybe deleted as per code review comments
    @SuppressWarnings("unused")
    public static final String ISR_DATA_QUALITY = "ext.isr.data-quality";

    // TODO i don't know how to get the data from metacard to output ISR_DATA_QUALITY, maybe deleted as per code review comments
    @SuppressWarnings("unused")
    public static final List<String> DATA_QUALITY_LIST = Arrays.asList(
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/MGMP_UsabilityElement",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_GriddedDataPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_AbsoluteExternalPositionalAccuracy");

    private static final String BOUNDING_BOX_FORMAT = "%.15f";

    private static final Logger LOGGER = LoggerFactory.getLogger(MgmpConverter.class);

    private static final int MIN_CLOUD_COVERAGE = 0;

    private static final int MAX_CLOUD_COVERAGE = 100;

    private static final String ID_REGEX = "^.{32}$";

    private static final int ID_SEGMENT_1_START = 0;

    private static final int ID_SEGMENT_1_END = 8;

    private static final int ID_SEGMENT_2_START = 8;

    private static final int ID_SEGMENT_2_END = 12;

    private static final int ID_SEGMENT_3_START = 12;

    private static final int ID_SEGMENT_3_END = 16;

    private static final int ID_SEGMENT_4_START = 16;

    private static final int ID_SEGMENT_4_END = 20;

    private static final int ID_SEGMENT_5_START = 20;

    private static final int ID_SEGMENT_5_END = 32;

    private static final String ID_SEPARATOR = "-";

    private Predicate<String> crsFilter = s -> s.matches("^[^:]+:[^:]+$");

    private int geographicElementIndex = 1;

    private int dateElementIndex = 1;

    private Supplier<String> gmlIdSupplier = this::generateGmlGuid;

    @Override
    protected List<String> getXstreamAliases() {
        return Arrays.asList(GmdConstants.GMD_LOCAL_NAME, MgmpConstants.GMD_METACARD_TYPE_NAME);
    }

    /**
     * Builds up the xml paths and values to write.
     * Order matters!  Paths should be added in the order they must be written.
     *
     * @param metacard the source
     * @return XstreamPathValueTracker containing XML paths and values to write
     */
    @Override
    protected XstreamPathValueTracker buildPaths(MetacardImpl metacard) {

        XstreamPathValueTracker pathValueTracker = new XstreamPathValueTracker();

        addNamespaces(pathValueTracker);

        addFileIdentifier(pathValueTracker, metacard);
        addCharacterSet(pathValueTracker);
        addHierarchyLevel(pathValueTracker);
        addContact(pathValueTracker, metacard);
        addDateStamp(pathValueTracker, metacard);
        addMetadataStandardName(pathValueTracker);
        addMetadataStandardVersion(pathValueTracker);
        addCrs(pathValueTracker, metacard);
        addMdIdentification(pathValueTracker, metacard);
        addContentInfo(pathValueTracker, metacard);
        addDistributionInfo(pathValueTracker, metacard);
        addMetadataConstraints(pathValueTracker, metacard);

        return pathValueTracker;
    }

    private void addMetadataConstraints(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addMetadataSecurityClassification(pathValueTracker, metacard);
        addOriginatorSecurity(pathValueTracker, metacard);
        addMetadataReleasability(pathValueTracker, metacard);
    }

    private void addDistributionInfo(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addGmdMetacardFormat(pathValueTracker, metacard);
        addGmdResourceUri(pathValueTracker, metacard);
    }

    private void addContentInfo(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        addImageDescription(pathValueTracker, metacard);
    }

    private void addMdIdentification(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addMdIdentificationCitation(pathValueTracker, metacard);
        addMdIdentificationAbstract(pathValueTracker, metacard);
        addMdIdentificationStatus(pathValueTracker, metacard);
        addMdIdentificationPointOfContact(pathValueTracker, metacard);
        addMdIdentificationDescriptiveKeywords(pathValueTracker, metacard);
        addMdIdentificationResourceConstraints(pathValueTracker, metacard);
        addMdIdentificationAggregationInfo(pathValueTracker, metacard);
        addMdIdentificationLanguage(pathValueTracker, metacard);
        addMdIdentificationTopicCategories(pathValueTracker, metacard);
        addMdIdentificationExtent(pathValueTracker, metacard);
    }

    private void addMdIdentificationExtent(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addGeographicIdentifier(pathValueTracker, metacard);
        addBoundingPolygon(pathValueTracker, metacard);
        addTemporalElements(pathValueTracker, metacard);
        addVerticalElement(pathValueTracker, metacard);
    }

    private void addMdIdentificationResourceConstraints(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addResourceSecurityClassification(pathValueTracker, metacard);
        addMdIdentificationResourceConstraintsOriginatorClassification(pathValueTracker, metacard);
        addMdIdentificationResourceConstraintsCaveats(pathValueTracker, metacard);
    }

    private void addMdIdentificationResourceConstraintsOriginatorClassification(
            XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Security.RESOURCE_ORIGINATOR_CLASSIFICATION,
                MgmpConstants.RESOURCE_ORIGINATOR_SECURITY_PATH,
                (pvt, mc) -> {
                    pvt.add(new Path(MgmpConstants.RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_PATH),
                            MgmpConstants.MGMP_CLASSIFICATION_CODE);
                });
    }

    private void addMdIdentificationCitation(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addGmdTitle(pathValueTracker, metacard);
        addModifiedDate(pathValueTracker, metacard);
        addCreatedDate(pathValueTracker, metacard);
        addExpirationDate(pathValueTracker, metacard);
    }

    private void addMdIdentificationLanguage(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        List<String> languageCodes = Utilities.getValues(metacard, Core.LANGUAGE);

        if (languageCodes.isEmpty()) {
            languageCodes = Collections.singletonList(Locale.ENGLISH.getISO3Language());
        }

        List<String> languageTexts = languageCodes.stream()
                .map(code -> code.equals(Locale.ENGLISH.getISO3Language()) ?
                        Locale.ENGLISH.getDisplayLanguage() :
                        "")
                .collect(Collectors.toList());

        List<String> languageCodeList = Utilities.replace(languageCodes,
                MgmpConstants.LANGUAGE_CODE_LIST);

        Utilities.addMultiValues(pathValueTracker,
                languageCodeList,
                MgmpConstants.MD_IDENTIFICATION_LANGUAGE_CODE_LIST_PATH);
        Utilities.addMultiValues(pathValueTracker,
                languageCodes,
                MgmpConstants.MD_IDENTIFICATION_LANGUAGE_CODE_LIST_VALUE_PATH);
        Utilities.addMultiValues(pathValueTracker,
                languageTexts,
                MgmpConstants.MD_IDENTIFICATION_LANGUAGE_TEXT_PATH);

    }

    private void addMdIdentificationAggregationInfo(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addMdIdentificationAggregationInfoAggregateDataSetIdentifier(pathValueTracker, metacard);
    }

    private void addMetadataStandardVersion(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(MgmpConstants.MD_METADATA_STANDARD_VERSION_PATH), "2.0");
    }

    private void addMetadataStandardName(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(MgmpConstants.MD_METADATA_STANDARD_NAME_PATH),
                "MOD Geospatial Metadata Profile");
    }

    private void addNamespaces(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(MgmpConstants.GMD_NAMESPACE_PATH),
                MgmpConstants.GMD_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.GCO_NAMESPACE_PATH),
                MgmpConstants.GCO_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.MGMP_NAMESPACE_PATH),
                MgmpConstants.MGMP_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.GML_NAMESPACE_PATH),
                MgmpConstants.GML_NAMESPACE);
        pathValueTracker.add(new Path(MgmpConstants.XLINK_NAMESPACE_PATH),
                MgmpConstants.XLINK_NAMESPACE);
    }

    private void addCharacterSet(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(MgmpConstants.CHARACTER_SET_CODE_LIST_PATH),
                MgmpConstants.MGMP_CHARACTER_SET_CODE);
        pathValueTracker.add(new Path(MgmpConstants.CHARACTER_SET_CODE_LIST_VALUE_PATH),
                MgmpConstants.ENCODING_TYPE);
        pathValueTracker.add(new Path(MgmpConstants.CHARACTER_SET_TEXT_PATH),
                MgmpConstants.ENCODING_DESCRIPTION);
    }

    private void addOriginatorSecurity(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Security.METADATA_ORIGINATOR_CLASSIFICATION,
                MgmpConstants.METADATA_ORIGINATOR_SECURITY_PATH,
                (pvt, mc) -> {
                    pvt.add(new Path(MgmpConstants.METADATA_ORIGINATOR_CLASSIFICATION_CODE_LIST_PATH),
                            MgmpConstants.CLASSIFICATION_CODE);
                });

    }

    private void addCrs(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {

        List<String> validCrsCodes = Utilities.getValues(metacard,
                Location.COORDINATE_REFERENCE_SYSTEM_CODE)
                .stream()
                .filter(crsFilter)
                .collect(Collectors.toList());

        Utilities.addMultiValues(pathValueTracker,
                validCrsCodes.stream()
                        .map(value -> value.split(":")[1])
                        .collect(Collectors.toList()),
                MgmpConstants.GMD_CRS_CODE_PATH);

        Utilities.addMultiValues(pathValueTracker,
                validCrsCodes.stream()
                        .map(value -> value.split(":")[0])
                        .collect(Collectors.toList()),
                MgmpConstants.GMD_CRS_AUTHORITY_PATH);

    }

    private void addBoundingPolygon(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.getValues(metacard, Core.LOCATION)
                .stream()
                .findFirst()
                .ifPresent(location -> {
                    try {
                        Geometry geometry = new WKTReader().read(location);

                        String str = Stream.of(geometry.getCoordinates())
                                .flatMap(coordinate -> Stream.of(coordinate.x, coordinate.y))
                                .map(d -> Double.toString(d))
                                .collect(Collectors.joining(" "));

                        pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.GMD_POLYGON_GMLID_PATH,
                                geographicElementIndex)), gmlIdSupplier.get());
                        pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.GMD_POLYGON_SRSNAME_PATH,
                                geographicElementIndex)), MgmpConstants.GMD_POLYGON_SRSNAME);

                        Utilities.addFieldIfString(pathValueTracker,
                                metacard,
                                mc -> {
                                    return Optional.of(str);
                                },
                                Utilities.replaceIndex(MgmpConstants.BOUNDING_POLYGON_PATH,
                                        geographicElementIndex));

                        geographicElementIndex++;

                        Coordinate[] coords = geometry.getCoordinates();

                        OptionalDouble westBoundLongitude = Stream.of(coords)
                                .mapToDouble(coord -> coord.x)
                                .min();

                        OptionalDouble eastBoundLongitude = Stream.of(coords)
                                .mapToDouble(coord -> coord.x)
                                .max();

                        OptionalDouble northBoundLatitude = Stream.of(coords)
                                .mapToDouble(coord -> coord.y)
                                .max();

                        OptionalDouble southBoundLatitude = Stream.of(coords)
                                .mapToDouble(coord -> coord.y)
                                .min();

                        boolean allSet = Stream.of(westBoundLongitude,
                                eastBoundLongitude,
                                northBoundLatitude,
                                southBoundLatitude)
                                .allMatch(OptionalDouble::isPresent);

                        if (allSet) {
                            addBoundingBoxElement(pathValueTracker,
                                    MgmpConstants.WEST_BOUND_LONGITUDE_PATH,
                                    westBoundLongitude);
                            addBoundingBoxElement(pathValueTracker,
                                    MgmpConstants.EAST_BOUND_LONGITUDE_PATH,
                                    eastBoundLongitude);
                            addBoundingBoxElement(pathValueTracker,
                                    MgmpConstants.SOUTH_BOUND_LATITUDE_PATH,
                                    southBoundLatitude);
                            addBoundingBoxElement(pathValueTracker,
                                    MgmpConstants.NORTH_BOUND_LATITUDE_PATH,
                                    northBoundLatitude);

                            geographicElementIndex++;
                        }

                    } catch (ParseException e) {
                        LOGGER.debug("unable to set location", e);
                    }
                });

    }

    private void addBoundingBoxElement(XstreamPathValueTracker pathValueTracker, String path,
            OptionalDouble optionalDouble) {
        pathValueTracker.add(new Path(Utilities.replaceIndex(path, geographicElementIndex)),
                String.format(BOUNDING_BOX_FORMAT, optionalDouble.getAsDouble()));
    }

    private void addGeographicIdentifier(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Location.COUNTRY_CODE),
                MgmpConstants.GMD_COUNTRY_CODE_PATH,
                geographicElementIndex);
        Utilities.addMultiValues(pathValueTracker,
                Utilities.replace(Utilities.getValues(metacard, Location.COUNTRY_CODE),
                        "ISO3166-1-a3"),
                MgmpConstants.GMD_COUNTRY_CODE_SPACE_PATH,
                geographicElementIndex);
        geographicElementIndex += Utilities.getValues(metacard, Location.COUNTRY_CODE)
                .size();
    }

    private void addMdIdentificationStatus(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                GmdConstants.RESOURCE_STATUS,
                GmdConstants.RESOURCE_STATUS_PATH,
                (pvt, mc) -> {
                    pvt.add(new Path(MgmpConstants.RESOURCE_STATUS_CODE_LIST_PATH),
                            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ProgressCode");
                });

    }

    private String generateGmlGuid() {
        return "GMLID_" + UUID.randomUUID()
                .toString();
    }

    private void addTemporalElements(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        List<String> starts = Utilities.getDateStrings(metacard, DateTime.START);
        List<String> ends = Utilities.getDateStrings(metacard, DateTime.END);

        if (starts.size() == ends.size()) {
            for (int i = 0; i < starts.size(); i++) {

                String start = starts.get(i);
                String end = ends.get(i);

                int elementIndex = i + 1;

                if (start.equals(end)) {
                    pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.GMD_TEMPORAL_TIME_INSTANT_ID_PATH,
                            elementIndex)), gmlIdSupplier.get());
                    pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.GMD_TEMPORAL_INSTANT_PATH,
                            elementIndex)), start);
                } else {
                    pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.GMD_TEMPORAL_TIME_PERIOD_ID_PATH,
                            elementIndex)), gmlIdSupplier.get());
                    pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.GMD_TEMPORAL_START_PATH,
                            elementIndex)), start);
                    pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.GMD_TEMPORAL_END_PATH,
                            elementIndex)), end);
                }
            }
        }
    }

    private void addVerticalElement(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        final AtomicBoolean addVerticalCRS = new AtomicBoolean(false);

        Utilities.addFieldIfString(pathValueTracker, metacard, mc -> {
            OptionalDouble optionalDouble = Utilities.getSerializables(mc, Location.ALTITUDE)
                    .stream()
                    .filter(Double.class::isInstance)
                    .mapToDouble(Double.class::cast)
                    .min();
            if (optionalDouble.isPresent()) {
                addVerticalCRS.set(true);
                return Optional.of(Double.toString(optionalDouble.getAsDouble()));
            }
            return Optional.empty();
        }, MgmpConstants.GMD_MIN_ALTITUDE_PATH);

        Utilities.addFieldIfString(pathValueTracker, metacard, mc -> {
            OptionalDouble optionalDouble = Utilities.getSerializables(mc, Location.ALTITUDE)
                    .stream()
                    .filter(Double.class::isInstance)
                    .mapToDouble(Double.class::cast)
                    .max();
            if (optionalDouble.isPresent()) {
                addVerticalCRS.set(true);
                return Optional.of(Double.toString(optionalDouble.getAsDouble()));
            }
            return Optional.empty();
        }, GmdConstants.ALTITUDE_PATH);

        if (addVerticalCRS.get()) {
            pathValueTracker.add(new Path(MgmpConstants.GMD_VERTICAL_CRS_XLINK_HREF_PATH),
                    "http://www.opengis.net/def/crs/EPSG/0/5701");
            pathValueTracker.add(new Path(MgmpConstants.GMD_VERTICAL_CRS_XLINK_TITLE_PATH),
                    "Newlyn Height");
        }
    }

    private void addCreatedDate(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                mc -> {
                    return Optional.ofNullable(mc.getCreatedDate())
                            .map(Utilities::dateToIso8601);
                },
                Utilities.replaceIndex(MgmpConstants.DATE_PATH, dateElementIndex),
                this::addCreatedDateExtra);
    }

    private void addCreatedDateExtra(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addDateTypeCode(pathValueTracker, GmdConstants.CREATION, "Creation");
        dateElementIndex++;
    }

    private void addExpirationDate(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                mc -> {
                    return Optional.ofNullable(mc.getExpirationDate())
                            .map(Utilities::dateToIso8601);
                },
                Utilities.replaceIndex(MgmpConstants.DATE_PATH, dateElementIndex),
                this::addExpirationDateExtra);
    }

    private void addExpirationDateExtra(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addDateTypeCode(pathValueTracker, GmdConstants.EXPIRY, "Expiry");
        dateElementIndex++;
    }

    private void addDateTypeCode(XstreamPathValueTracker pathValueTracker, String dateTypeCodeValue,
            String dateTypeText) {
        pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.DATE_TYPE_CODE_VALUE_PATH,
                dateElementIndex)), dateTypeCodeValue);
        pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.DATE_TYPE_CODE_PATH,
                dateElementIndex)), "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_DateTypeCode");
        pathValueTracker.add(new Path(Utilities.replaceIndex(MgmpConstants.DATE_TYPE_CODE_TEXT_PATH,
                dateElementIndex)), dateTypeText);
    }

    private void addModifiedDate(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                mc -> {
                    return Optional.ofNullable(mc.getModifiedDate())
                            .map(Utilities::dateToIso8601);
                },
                Utilities.replaceIndex(MgmpConstants.DATE_PATH, dateElementIndex),
                this::addModifiedDateExtra);
    }

    private void addModifiedDateExtra(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addDateTypeCode(pathValueTracker, GmdConstants.LAST_UPDATE, "LastUpdate");
        dateElementIndex++;
    }

    private void addDateStamp(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {

        Optional<Date> modified = Utilities.getOptionalDate(metacard, Core.METACARD_MODIFIED);
        Optional<Date> created = Utilities.getOptionalDate(metacard, Core.METACARD_CREATED);

        Optional<Date> date = modified.isPresent() ?
                modified :
                (created.isPresent() ? created : Optional.empty());

        date.ifPresent(date1 -> pathValueTracker.add(new Path(GmdConstants.DATE_TIME_STAMP_PATH),
                Utilities.dateToIso8601(date1)));
    }

    private void addMdIdentificationPointOfContact(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_NAME),
                MgmpConstants.POINT_OF_CONTACT_NAME_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_PHONE),
                MgmpConstants.POINT_OF_CONTACT_PHONE_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_ADDRESS),
                MgmpConstants.POINT_OF_CONTACT_ADDRESS_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_EMAIL),
                MgmpConstants.POINT_OF_CONTACT_EMAIL_PATH);

        pathValueTracker.add(new Path(MgmpConstants.POINT_OF_CONTACT_ROLE_CODE_LIST_PATH),
                MgmpConstants.MGMP_ROLE_CODE);
        pathValueTracker.add(new Path(MgmpConstants.POINT_OF_CONTACT_ROLE_CODE_LIST_VALUE_PATH),
                "originator");
        pathValueTracker.add(new Path(MgmpConstants.POINT_OF_CONTACT_ROLE_TEXT_PATH), "Originator");

    }

    private void addContact(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {

        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Contact.PUBLISHER_NAME,
                GmdConstants.CONTACT_ORGANISATION_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.PUBLISHER_PHONE),
                MgmpConstants.GMD_CONTACT_PHONE_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.PUBLISHER_ADDRESS),
                MgmpConstants.GMD_POINT_OF_CONTACT_ADDRESS_DELIVERY_POINT_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.PUBLISHER_EMAIL),
                MgmpConstants.GMD_CONTACT_EMAIL_PATH);

        pathValueTracker.add(new Path(MgmpConstants.CONTACT_ROLE_CODE_LIST_PATH),
                MgmpConstants.MGMP_ROLE_CODE);
        pathValueTracker.add(new Path(MgmpConstants.CONTACT_ROLE_CODE_LIST_VALUE_PATH),
                "pointOfContact");
        pathValueTracker.add(new Path(MgmpConstants.CONTACT_ROLE_TEXT_PATH), "Point of Contact");

    }

    private void addGmdResourceUri(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, mc -> {
            return Optional.ofNullable(mc.getResourceURI())
                    .map(URI::toString);
        }, GmdConstants.LINKAGE_URI_PATH);
    }

    private String formatId(String id) {
        if (id.matches(ID_REGEX)) {
            return id.substring(ID_SEGMENT_1_START, ID_SEGMENT_1_END) + ID_SEPARATOR + id.substring(
                    ID_SEGMENT_2_START,
                    ID_SEGMENT_2_END) + ID_SEPARATOR + id.substring(ID_SEGMENT_3_START,
                    ID_SEGMENT_3_END) + ID_SEPARATOR + id.substring(ID_SEGMENT_4_START,
                    ID_SEGMENT_4_END) + ID_SEPARATOR + id.substring(ID_SEGMENT_5_START,
                    ID_SEGMENT_5_END);
        }
        return id;
    }

    private void addMdIdentificationAggregationInfoAggregateDataSetIdentifier(
            XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {

        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                mc -> {
                    return Optional.ofNullable(mc.getAttribute(Associations.RELATED))
                            .map(Attribute::getValue)
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .map(this::formatId);
                },
                GmdConstants.ASSOCIATION_PATH,
                this::addMdIdentificationAggregationInfoAggregateDataSetIdentifierExtra);
    }

    private void addMdIdentificationAggregationInfoAggregateDataSetIdentifierExtra(
            XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_CODE_SPACE_PATH),
                "mediaReferenceNo");
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_TYPE_CODE_LIST_PATH),
                MgmpConstants.MGMP_ASSOCIATION_TYPE_CODE);
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_TYPE_CODE_LIST_VALUE_PATH),
                "mediaAssociation");
        pathValueTracker.add(new Path(MgmpConstants.ASSOCIATIONS_RELATED_TYPE_TEXT_PATH),
                "Media Association");
    }

    private void addMdIdentificationTopicCategories(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Topic.CATEGORY),
                MgmpConstants.GMD_TOPIC_CATEGORY_PATH);
    }

    private void addMdIdentificationDescriptiveKeywords(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Topic.KEYWORD),
                MgmpConstants.GMD_KEYWORD_PATH);
    }

    private void addGmdMetacardFormat(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Media.FORMAT,
                GmdConstants.FORMAT_PATH);
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Media.FORMAT_VERSION,
                GmdConstants.FORMAT_VERSION_PATH);
    }

    private void addMdIdentificationAbstract(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Core.DESCRIPTION,
                GmdConstants.ABSTRACT_PATH);
    }

    private void addGmdTitle(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, mc -> {
            return Optional.of(StringUtils.defaultString(mc.getTitle()));
        }, GmdConstants.TITLE_PATH);
    }

    private void addFileIdentifier(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, mc -> {
            return Optional.of(StringUtils.defaultString(mc.getId()))
                    .map(this::formatId);
        }, GmdConstants.FILE_IDENTIFIER_PATH);
    }

    private void addHierarchyLevel(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(GmdConstants.CODE_LIST_VALUE_PATH), "dataset");
        pathValueTracker.add(new Path(GmdConstants.CODE_LIST_PATH), MgmpConstants.MGMP_SCOPE_CODE);
    }

    @Override
    protected String getRootNodeName() {
        return GmdConstants.GMD_LOCAL_NAME;
    }

    private void addMetadataReleasability(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Attribute releasibilityAttribute = metacard.getAttribute(Security.METADATA_RELEASABILITY);
        Attribute disseminationAttribute = metacard.getAttribute(Security.METADATA_DISSEMINATION);

        if (isReleasabilityAndDisseminationSet(releasibilityAttribute, disseminationAttribute)) {

            String value = disseminationAttribute.getValue()
                    .toString() + " " + Utilities.join(releasibilityAttribute.getValues(), "/");

            pathValueTracker.add(new Path(MgmpConstants.METADATA_RELEASABILITY_PATH), value);

        }

    }

    private boolean isReleasabilityAndDisseminationSet(Attribute releasibilityAttribute,
            Attribute disseminationAttribute) {
        return releasibilityAttribute != null && disseminationAttribute != null
                && releasibilityAttribute.getValues() != null && !releasibilityAttribute.getValues()
                .isEmpty() &&
                disseminationAttribute.getValue() != null
                && StringUtils.isNotBlank((String) disseminationAttribute.getValue());
    }

    private void addMdIdentificationResourceConstraintsCaveats(
            XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {

        Attribute releasibilityAttribute = metacard.getAttribute(Security.RESOURCE_RELEASABILITY);
        Attribute disseminationAttribute = metacard.getAttribute(Security.RESOURCE_DISSEMINATION);

        if (isReleasabilityAndDisseminationSet(releasibilityAttribute, disseminationAttribute)) {

            String value = disseminationAttribute.getValue()
                    .toString() + " " + Utilities.join(releasibilityAttribute.getValues(), "/");

            pathValueTracker.add(new Path(MgmpConstants.RESOURCE_SECURITY_RELEASABILITY_PATH),
                    value);

        }
    }

    private void addResourceSecurityClassification(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Security.RESOURCE_CLASSIFICATION,
                MgmpConstants.RESOURCE_SECURITY_PATH,
                (pvt, mc) -> {
                    pvt.add(new Path(MgmpConstants.RESOURCE_SECURITY_CODE_LIST_PATH),
                            MgmpConstants.CLASSIFICATION_CODE);
                });
    }

    private void addMetadataSecurityClassification(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Security.METADATA_CLASSIFICATION,
                MgmpConstants.METADATA_SECURITY_PATH,
                (pvt, mc) -> {
                    pvt.add(new Path(MgmpConstants.METADATA_SECURITY_CODE_LIST_PATH),
                            MgmpConstants.CLASSIFICATION_CODE);
                });
    }

    private void addImageDescription(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        Optional<String> cloudCoverage = Optional.ofNullable(metacard.getAttribute(Isr.CLOUD_COVER))
                .map(Attribute::getValue)
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast)
                .filter(integer -> integer >= MIN_CLOUD_COVERAGE && integer <= MAX_CLOUD_COVERAGE)
                .map(integer -> Integer.toString(integer));

        boolean isCloudCoverageAvailable = cloudCoverage.isPresent();

        List<Serializable> ratingScaleValues = Utilities.getSerializables(metacard,
                Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE);

        boolean isRatingScaleValuesAvailable = ratingScaleValues.size() == 1;

        if (isCloudCoverageAvailable || isRatingScaleValuesAvailable) {

            List<String> attributeDescription = Stream.of(metacard.getAttribute(Isr.COMMENTS))
                    .flatMap(attribute -> attribute.getValues()
                            .stream())
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());

            if (attributeDescription.size() != 1) {
                pathValueTracker.add(new Path(MgmpConstants.CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_REASON_PATH),
                        "unknown");
            } else {
                pathValueTracker.add(new Path(MgmpConstants.ISR_IMAGE_COMMENT_PATH),
                        attributeDescription.get(0));
            }

            pathValueTracker.add(new Path(MgmpConstants.CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_TEXT_PATH),
                    "Image");
            pathValueTracker.add(new Path(MgmpConstants.CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_PATH),
                    MgmpConstants.MGMP_COVERAGE_CONTENT_TYPE_CODE);
            pathValueTracker.add(new Path(MgmpConstants.ISR_IMAGE_DESCRIPTION_PATH), "image");
        }

        if (!ratingScaleValues.isEmpty()) {
            ratingScaleValues.stream()
                    .map(Object::toString)
                    .findFirst()
                    .ifPresent(value -> {
                        pathValueTracker.add(new Path(MgmpConstants.NIIRS_RATING_PATH),
                                MgmpConstants.NIIRS);
                        pathValueTracker.add(new Path(MgmpConstants.NIIRS_PATH), value);
                    });

        }

        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                mc -> cloudCoverage,
                MgmpConstants.CLOUD_COVERAGE_PATH);
    }

    public void setGmlIdSupplier(Supplier<String> gmlIdSupplier) {
        this.gmlIdSupplier = gmlIdSupplier;
    }

    private static class Utilities {

        private static List<String> replace(List<String> list, String replacement) {
            return Collections.nCopies(list.size(), replacement);
        }

        private static Optional<Date> getOptionalDate(MetacardImpl metacard, String dateField) {
            return Optional.ofNullable(metacard.getAttribute(dateField))
                    .map(Attribute::getValue)
                    .filter(Date.class::isInstance)
                    .map(Date.class::cast);
        }

        private static <T> String join(List<T> values, String joiner) {
            return values.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(joiner));
        }

        private static String replaceIndex(String template, int index) {
            return template.replace(MgmpConstants.INDEX_TAG, Integer.toString(index));
        }

        private static List<Serializable> getSerializables(MetacardImpl metacard,
                String attributeName) {

            Attribute attribute = metacard.getAttribute(attributeName);

            if (attribute == null) {
                return Collections.emptyList();
            }

            List<Serializable> serializables = attribute.getValues();

            if (serializables == null) {
                return Collections.emptyList();
            }

            return serializables.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        private static List<String> getValues(MetacardImpl metacard, String attributeName) {
            return getSerializables(metacard, attributeName).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        }

        private static void addMultiValues(XstreamPathValueTracker pathValueTracker,
                List<String> values, String xpathTemplate) {
            addMultiValues(pathValueTracker, values, xpathTemplate, 1);
        }

        private static void addMultiValues(XstreamPathValueTracker pathValueTracker,
                List<String> values, String xpathTemplate, int startIndex) {
            int index = startIndex;
            for (String value : values) {
                pathValueTracker.add(new Path(Utilities.replaceIndex(xpathTemplate, index)), value);
                index++;
            }

        }

        private static List<String> getDateStrings(MetacardImpl metacard, String attributeName) {
            return Utilities.getSerializables(metacard, attributeName)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(Date.class::isInstance)
                    .map(Date.class::cast)
                    .map(Utilities::dateToIso8601)
                    .collect(Collectors.toList());
        }

        /**
         * @param pathValueTracker path value tracker
         * @param metacard         the metacard
         * @param attributeName    the attribute source
         * @param xpath            the xpath destination
         * @param extraAdd         execute this BiConsumer if the value string is being applied to the pathValueTracker
         * @return true if the value was added, false otherwise
         */
        private static boolean addFieldIfString(XstreamPathValueTracker pathValueTracker,
                MetacardImpl metacard, String attributeName, String xpath,
                BiConsumer<XstreamPathValueTracker, MetacardImpl> extraAdd) {
            return addFieldIfString(pathValueTracker, metacard, mc -> {
                return Optional.of(mc)
                        .map(m -> m.getAttribute(attributeName))
                        .map(Attribute::getValue)
                        .filter(String.class::isInstance)
                        .map(String.class::cast);
            }, xpath, extraAdd);
        }

        private static boolean addFieldIfString(XstreamPathValueTracker pathValueTracker,
                MetacardImpl metacard, String attributeName, String xpath) {
            return addFieldIfString(pathValueTracker, metacard, attributeName, xpath, (pvt, mc) -> {
            });
        }

        private static boolean addFieldIfString(XstreamPathValueTracker pathValueTracker,
                MetacardImpl metacard, Function<MetacardImpl, Optional<String>> metacardFunction,
                String xpath, BiConsumer<XstreamPathValueTracker, MetacardImpl> extraAdd) {
            return metacardFunction.apply(metacard)
                    .map(value -> {
                        pathValueTracker.add(new Path(xpath), value);
                        extraAdd.accept(pathValueTracker, metacard);
                        return true;
                    })
                    .orElse(false);
        }

        private static boolean addFieldIfString(XstreamPathValueTracker pathValueTracker,
                MetacardImpl metacard, Function<MetacardImpl, Optional<String>> metacardFunction,
                String xpath) {
            return addFieldIfString(pathValueTracker,
                    metacard,
                    metacardFunction,
                    xpath,
                    (pvt, mc) -> {
                    });
        }

        private static String dateToIso8601(Date date) {
            GregorianCalendar modifiedCal = new GregorianCalendar();
            if (date != null) {
                modifiedCal.setTime(date);
            }
            modifiedCal.setTimeZone(UTC_TIME_ZONE);

            return XSD_FACTORY.newXMLGregorianCalendar(modifiedCal)
                    .toXMLFormat();
        }

    }

}