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
package org.codice.alliance.catalog.mgmp.converter;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.types.Isr;
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

    public static final String RESOURCE_SECURITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeList";

    public static final String RESOURCE_SECURITY_RELEASABILITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    public static final String RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeList";

    public static final String RESOURCE_STATUS_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/status/MD_ProgressCode/@codeList";

    public static final String GMD_MAX_ALTITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/maximumValue/gco:Real";

    public static final String GMD_MIN_ALTITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/minimumValue/gco:Real";

    public static final String GMD_VERTICAL_CRS_XLINK_HREF_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/@xlink:href";

    public static final String GMD_VERTICAL_CRS_XLINK_TITLE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/@xlink:title";

    public static final String GMD_ASSOCIATION_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/aggregateDataSetIdentifier/RS_Identifier/code/gco:CharacterString";

    public static final String ASSOCIATIONS_RELATED_CODE_SPACE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/aggregateDataSetIdentifier/RS_Identifier/codeSpace/gco:CharacterString";

    public static final String ASSOCIATIONS_RELATED_TYPE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode/@codeList";

    public static final String ASSOCIATIONS_RELATED_TYPE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode/@codeListValue";

    public static final String ASSOCIATIONS_RELATED_TYPE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode";

    public static final String RESOURCE_STATUS_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/status/MD_ProgressCode/@codeListValue";

    public static final String ABSTRACT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/abstract/gco:CharacterString";

    public static final String TITLE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/title/gco:CharacterString";

    public static final String SECURITY_METADATA_CLASSIFICATION_ATTRIBUTE =
            "ext.security.metadata-classification";

    public static final String SECURITY_METADATA_RELEASABILITY_ATTRIBUTE =
            "ext.security.metadata-releasability";

    public static final String SECURITY_RESOURCE_CLASSIFICATION_ATTRIBUTE =
            "ext.security.resource-classification";

    public static final String SECURITY_METADATA_DISSEMINATION_ATTRIBUTE =
            "ext.security.metadata-dissemination-controls";

    public static final String SECURITY_METADATA_ORIGINATOR_CLASSIFICATION =
            "ext.security.metadata-originator-classification";

    public static final String SECURITY_RESOURCE_ORIGINATOR_CLASSIFICATION =
            "ext.security.resource-originator-classification";

    public static final String METATADA_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    public static final String METATADA_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeList";

    public static final String METADATA_SECURITY_RELEASABILITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    public static final String METADATA_ORIGINATOR_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    public static final String METADATA_ORIGINATOR_CLASSIFICATION_CODE_LIST_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeList";

    public static final String SECURITY_RESOURCE_DISSEMINATION =
            "ext.security.resource-dissemination-controls";

    public static final String RESOURCE_RATING_SYSTEM = "ext.resource-rating-system";

    public static final String RESOURCE_RATING = "ext.resource-rating";

    public static final String GMD_POLYGON_GMLID = "GMLID_eea1bec0-9aaf-11e5-9cdf-0002a5d5c51b";

    public static final String GMD_POLYGON_SRSNAME = "http://www.opengis.net/def/crs/EPSG/0/4326";

    public static final String SECURITY_RESOURCE_RELEASABILITY =
            "ext.security.resource-releasability";

    public static final String GMD_NAMESPACE_PATH = "/MD_Metadata/@xmlns";

    public static final String GCO_PREFIX = "gco";

    public static final String GCO_NAMESPACE_PATH = "/MD_Metadata/@xmlns:" + GCO_PREFIX;

    public static final String MGMP_NAMESPACE_PATH = "/MD_Metadata/@xmlns:mgmp";

    public static final String MGMP_NAMESPACE = "http://mod.uk/spatial/ns/mgmp/2.0";

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

    public static final String CLASSIFICATION_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ClassificationUK_GovCode";

    public static final String CHARACTER_SET_CODE_LIST_PATH =
            "/MD_Metadata/characterSet/MD_CharacterSetCode/@codeList";

    public static final String CHARACTER_SET_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/characterSet/MD_CharacterSetCode/@codeListValue";

    public static final String CHARACTER_SET_TEXT_PATH =
            "/MD_Metadata/characterSet/MD_CharacterSetCode";

    public static final String CONTACT_ROLE_CODE_LIST_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/role/CI_RoleCode/@codeList";

    public static final String CONTACT_ROLE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/role/CI_RoleCode/@codeListValue";

    public static final String CONTACT_ROLE_TEXT_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/role/CI_RoleCode";

    public static final String GMD_CONTACT_ORGANISATION_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/organisationName/gco:CharacterString";

    public static final String LANGUAGE_CODE_LIST =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_LanguageCode";

    public static final String GMD_METACARD_TYPE_NAME = "gmd.MD_Metadata";

    public static final String GMD_NAMESPACE = "http://www.isotc211.org/2005/gmd";

    public static final String GCO_NAMESPACE = "http://www.isotc211.org/2005/gco";

    public static final String RESOURCE_STATUS = "ext.resource-status";

    public static final String DATE_TIME_STAMP_PATH = "/MD_Metadata/dateStamp/gco:DateTime";

    public static final String LINKAGE_URI_PATH =
            "/MD_Metadata/distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine/CI_OnlineResource/linkage/URL";

    public static final String FORMAT_PATH =
            "/MD_Metadata/distributionInfo/MD_Distribution/distributionFormat/MD_Format/name/gco:CharacterString";

    public static final String FORMAT_VERSION_PATH =
            "/MD_Metadata/distributionInfo/MD_Distribution/distributionFormat/MD_Format/version/gco:CharacterString";

    public static final String FILE_IDENTIFIER_PATH =
            "/MD_Metadata/fileIdentifier/gco:CharacterString";

    public static final String CODE_LIST_VALUE_PATH =
            "/MD_Metadata/hierarchyLevel/MD_ScopeCode/@codeListValue";

    public static final String CODE_LIST_PATH =
            "/MD_Metadata/hierarchyLevel/MD_ScopeCode/@codeList";

    public static final String GMD_LOCAL_NAME = "MD_Metadata";

    public static final String POINT_OF_CONTACT_ROLE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode/@codeList";

    public static final String POINT_OF_CONTACT_ROLE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode/@codeListValue";

    public static final String POINT_OF_CONTACT_ROLE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode";

    public static final String CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/cloudCoverPercentage/gco:Real";

    public static final String BOUNDING_BOX_FORMAT = "%.15f";

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_DESC_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/attributeDescription/gco:RecordType";

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_TEXT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode";

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeList";

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_REASON_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/attributeDescription/@gco:nilReason";

    private static final String INDEX_TAG = "%index%";

    public static final String SOUTH_BOUND_LATITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/southBoundLatitude/gco:Decimal";

    public static final String EAST_BOUND_LONGITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/eastBoundLongitude/gco:Decimal";

    public static final String WEST_BOUND_LONGITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/westBoundLongitude/gco:Decimal";

    public static final String NORTH_BOUND_LATITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_GeographicBoundingBox/northBoundLatitude/gco:Decimal";

    public static final String MD_IDENTIFICATION_LANGUAGE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language[" + INDEX_TAG
                    + "]/LanguageCode/@codeList";

    public static final String MD_IDENTIFICATION_LANGUAGE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language[" + INDEX_TAG
                    + "]/LanguageCode/@codeListValue";

    public static final String MD_IDENTIFICATION_LANGUAGE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language[" + INDEX_TAG
                    + "]/LanguageCode";

    public static final String GMD_TEMPORAL_START_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimePeriod/gml:beginPosition";

    public static final String GMD_TEMPORAL_END_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimePeriod/gml:endPosition";

    public static final String GMD_TEMPORAL_TIME_PERIOD_ID_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimePeriod/@gml:id";

    public static final String GMD_TEMPORAL_INSTANT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimeInstant/gml:timePosition";

    public static final String GMD_TEMPORAL_TIME_INSTANT_ID_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/temporalElement["
                    + INDEX_TAG + "]/EX_TemporalExtent/extent/gml:TimeInstant/@gml:id";

    public static final String BOUNDING_POLYGON_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG
                    + "]/EX_BoundingPolygon/polygon/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList";

    public static final String GMD_TOPIC_CATEGORY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/topicCategory[" + INDEX_TAG
                    + "]/MD_TopicCategoryCode";

    public static final String GMD_KEYWORD_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/descriptiveKeywords/MD_Keywords/keyword["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String GMD_COUNTRY_CODE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG
                    + "]/EX_GeographicDescription/geographicIdentifier/RS_Identifier/code/gco:CharacterString";

    public static final String GMD_COUNTRY_CODE_SPACE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG
                    + "]/EX_GeographicDescription/geographicIdentifier/RS_Identifier/codeSpace/gco:CharacterString";

    public static final String GMD_POLYGON_GMLID_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_BoundingPolygon/polygon/gml:Polygon/@gml:id";

    public static final String GMD_POLYGON_SRSNAME_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement["
                    + INDEX_TAG + "]/EX_BoundingPolygon/polygon/gml:Polygon/@srsName";

    public static final String DATE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/date/gco:DateTime";

    public static final String DATE_TYPE_CODE_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/dateType/CI_DateTypeCode/@codeListValue";

    public static final String DATE_TYPE_CODE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/dateType/CI_DateTypeCode/@codeList";

    public static final String DATE_TYPE_CODE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/date["
                    + INDEX_TAG + "]/CI_Date/dateType/CI_DateTypeCode";

    public static final String POINT_OF_CONTACT_NAME_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/organisationName["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String POINT_OF_CONTACT_ADDRESS_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/deliveryPoint["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String POINT_OF_CONTACT_EMAIL_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/electronicMailAddress["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String POINT_OF_CONTACT_PHONE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/contactInfo/CI_Contact/phone/CI_Telephone/voice["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String GMD_CONTACT_PHONE_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/phone/CI_Telephone/voice["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String GMD_CONTACT_EMAIL_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/electronicMailAddress["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String GMD_POINT_OF_CONTACT_ADDRESS_DELIVERY_POINT_PATH =
            "/MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/deliveryPoint["
                    + INDEX_TAG + "]/gco:CharacterString";

    public static final String GMD_CRS_AUTHORITY_PATH =
            "/MD_Metadata/referenceSystemInfo[" + INDEX_TAG
                    + "]/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier/codeSpace/gco:CharacterString";

    public static final String GMD_CRS_CODE_PATH = "/MD_Metadata/referenceSystemInfo[" + INDEX_TAG
            + "]/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier/code/gco:CharacterString";

    public static final String RESOURCE_RATING_CODE_SPACE_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode[" + INDEX_TAG
                    + "]/RS_Identifier/codeSpace/gco:CharacterString";

    public static final String RESOURCE_RATING_CODE_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode[" + INDEX_TAG
                    + "]/RS_Identifier/code/gco:CharacterString";

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

    @Override
    protected List<String> getXstreamAliases() {
        return Arrays.asList(GMD_LOCAL_NAME, GMD_METACARD_TYPE_NAME);
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
        addMetadataSercurityClassification(pathValueTracker, metacard);
        addOriginatorSecurity(pathValueTracker, metacard);
        addMetadataReleasability(pathValueTracker, metacard);
    }

    private void addDistributionInfo(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addGmdMetacardFormat(pathValueTracker, metacard);
        addGmdResourceUri(pathValueTracker, metacard);
    }

    @SuppressWarnings("UnusedParameters")
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
        addResourceSercurityClassification(pathValueTracker, metacard);
        addMdIdentificationResourceConstraintsOriginatorClassification(pathValueTracker, metacard);
        addMdIdentificationResourceConstraintsCaveats(pathValueTracker, metacard);
    }

    private void addMdIdentificationResourceConstraintsOriginatorClassification(
            XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                SECURITY_RESOURCE_ORIGINATOR_CLASSIFICATION,
                RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_VALUE_PATH,
                (pathValueTracker1, metacard1) -> {
                    pathValueTracker1.add(new Path(RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_PATH),
                            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ClassificationCode");
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
            languageCodes = Collections.singletonList("eng");
        }

        List<String> languageTexts = languageCodes.stream()
                .map(code -> code.equals("eng") ? "English" : "")
                .collect(Collectors.toList());

        List<String> languageCodeList = Utilities.replace(languageCodes, LANGUAGE_CODE_LIST);

        Utilities.addMultiValues(pathValueTracker,
                languageCodeList,
                MD_IDENTIFICATION_LANGUAGE_CODE_LIST_PATH);
        Utilities.addMultiValues(pathValueTracker,
                languageCodes,
                MD_IDENTIFICATION_LANGUAGE_CODE_LIST_VALUE_PATH);
        Utilities.addMultiValues(pathValueTracker,
                languageTexts,
                MD_IDENTIFICATION_LANGUAGE_TEXT_PATH);

    }

    private void addMdIdentificationAggregationInfo(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addMdIdentificationAggregationInfoAggregateDataSetIdentifier(pathValueTracker, metacard);
    }

    private void addMetadataStandardVersion(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path("/MD_Metadata/metadataStandardVersion/gco:CharacterString"),
                "2.0");
    }

    private void addMetadataStandardName(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path("/MD_Metadata/metadataStandardName/gco:CharacterString"),
                "MOD Geospatial Metadata Profile");
    }

    private void addNamespaces(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(GMD_NAMESPACE_PATH), GMD_NAMESPACE);
        pathValueTracker.add(new Path(GCO_NAMESPACE_PATH), GCO_NAMESPACE);
        pathValueTracker.add(new Path(MGMP_NAMESPACE_PATH), MGMP_NAMESPACE);
        pathValueTracker.add(new Path("/MD_Metadata/@xmlns:gml"), "http://www.opengis.net/gml/3.2");
        pathValueTracker.add(new Path("/MD_Metadata/@xmlns:xlink"), "http://www.w3.org/1999/xlink");
    }

    private void addCharacterSet(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(CHARACTER_SET_CODE_LIST_PATH),
                "http://mod.uk/spatial/codelist/mgmp/2.0/ MGMP_CharacterSetCode");
        pathValueTracker.add(new Path(CHARACTER_SET_CODE_LIST_VALUE_PATH), "utf8");
        pathValueTracker.add(new Path(CHARACTER_SET_TEXT_PATH), "UTF-8");
    }

    private void addOriginatorSecurity(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                SECURITY_METADATA_ORIGINATOR_CLASSIFICATION,
                METADATA_ORIGINATOR_SECURITY_PATH,
                (pathValueTracker1, metacard1) -> {
                    pathValueTracker1.add(new Path(METADATA_ORIGINATOR_CLASSIFICATION_CODE_LIST_PATH),
                            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ClassificationUK_GovCode");
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
                GMD_CRS_CODE_PATH);

        Utilities.addMultiValues(pathValueTracker,
                validCrsCodes.stream()
                        .map(value -> value.split(":")[0])
                        .collect(Collectors.toList()),
                GMD_CRS_AUTHORITY_PATH);

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

                        pathValueTracker.add(new Path(Utilities.replaceIndex(GMD_POLYGON_GMLID_PATH,
                                geographicElementIndex)), GMD_POLYGON_GMLID);
                        pathValueTracker.add(new Path(Utilities.replaceIndex(
                                GMD_POLYGON_SRSNAME_PATH,
                                geographicElementIndex)), GMD_POLYGON_SRSNAME);

                        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
                            return Optional.of(str);
                        }, Utilities.replaceIndex(BOUNDING_POLYGON_PATH, geographicElementIndex));

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
                                    WEST_BOUND_LONGITUDE_PATH,
                                    westBoundLongitude);
                            addBoundingBoxElement(pathValueTracker,
                                    EAST_BOUND_LONGITUDE_PATH,
                                    eastBoundLongitude);
                            addBoundingBoxElement(pathValueTracker,
                                    SOUTH_BOUND_LATITUDE_PATH,
                                    southBoundLatitude);
                            addBoundingBoxElement(pathValueTracker,
                                    NORTH_BOUND_LATITUDE_PATH,
                                    northBoundLatitude);
                        }

                        geographicElementIndex++;

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
                GMD_COUNTRY_CODE_PATH,
                geographicElementIndex);
        Utilities.addMultiValues(pathValueTracker,
                Utilities.replace(Utilities.getValues(metacard, Location.COUNTRY_CODE),
                        "ISO3166-1-a3"),
                GMD_COUNTRY_CODE_SPACE_PATH,
                geographicElementIndex);
        geographicElementIndex += Utilities.getValues(metacard, Location.COUNTRY_CODE)
                .size();
    }

    private void addMdIdentificationStatus(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                RESOURCE_STATUS,
                RESOURCE_STATUS_PATH,
                (pathValueTracker1, metacard1) -> {
                    pathValueTracker1.add(new Path(RESOURCE_STATUS_CODE_LIST_PATH),
                            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ProgressCode");
                });

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
                    pathValueTracker.add(new Path(Utilities.replaceIndex(
                            GMD_TEMPORAL_TIME_INSTANT_ID_PATH,
                            elementIndex)), "GMLID_c1dcde60-9aaf-11e5-a565-0002a5d5c51b");
                    pathValueTracker.add(new Path(Utilities.replaceIndex(GMD_TEMPORAL_INSTANT_PATH,
                            elementIndex)), start);
                } else {
                    pathValueTracker.add(new Path(Utilities.replaceIndex(
                            GMD_TEMPORAL_TIME_PERIOD_ID_PATH,
                            elementIndex)), "GMLID_b3bc8920-9aaf-11e5-967d-0002a5d5c51b");
                    pathValueTracker.add(new Path(Utilities.replaceIndex(GMD_TEMPORAL_START_PATH,
                            elementIndex)), start);
                    pathValueTracker.add(new Path(Utilities.replaceIndex(GMD_TEMPORAL_END_PATH,
                            elementIndex)), end);
                }
            }
        }
    }

    private void addVerticalElement(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        final AtomicBoolean addVerticalCRS = new AtomicBoolean(false);

        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            OptionalDouble optionalDouble = Utilities.getSerializables(metacard1, Location.ALTITUDE)
                    .stream()
                    .filter(Double.class::isInstance)
                    .mapToDouble(Double.class::cast)
                    .min();
            if (optionalDouble.isPresent()) {
                addVerticalCRS.set(true);
                return Optional.of(Double.toString(optionalDouble.getAsDouble()));
            }
            return Optional.empty();
        }, GMD_MIN_ALTITUDE_PATH);

        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            OptionalDouble optionalDouble = Utilities.getSerializables(metacard1, Location.ALTITUDE)
                    .stream()
                    .filter(Double.class::isInstance)
                    .mapToDouble(Double.class::cast)
                    .max();
            if (optionalDouble.isPresent()) {
                addVerticalCRS.set(true);
                return Optional.of(Double.toString(optionalDouble.getAsDouble()));
            }
            return Optional.empty();
        }, GMD_MAX_ALTITUDE_PATH);

        if (addVerticalCRS.get()) {
            pathValueTracker.add(new Path(GMD_VERTICAL_CRS_XLINK_HREF_PATH),
                    "http://ww.opengis.net/def/crs/EPSG/0/5701");
            pathValueTracker.add(new Path(GMD_VERTICAL_CRS_XLINK_TITLE_PATH), "Newlyn Height");
        }
    }

    private void addCreatedDate(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            return Optional.ofNullable(metacard1.getCreatedDate())
                    .map(Utilities::dateToIso8601);
        }, Utilities.replaceIndex(DATE_PATH, dateElementIndex), this::addCreatedDateExtra);
    }

    private void addCreatedDateExtra(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addDateTypeCode(pathValueTracker, "creation", "Creation");
        dateElementIndex++;
    }

    private void addExpirationDate(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            return Optional.ofNullable(metacard1.getExpirationDate())
                    .map(Utilities::dateToIso8601);
        }, Utilities.replaceIndex(DATE_PATH, dateElementIndex), this::addExpirationDateExtra);
    }

    private void addExpirationDateExtra(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addDateTypeCode(pathValueTracker, "expiry", "Expiry");
        dateElementIndex++;
    }

    private void addDateTypeCode(XstreamPathValueTracker pathValueTracker, String dateTypeCodeValue,
            String dateTypeText) {
        pathValueTracker.add(new Path(Utilities.replaceIndex(DATE_TYPE_CODE_VALUE_PATH,
                dateElementIndex)), dateTypeCodeValue);
        pathValueTracker.add(new Path(Utilities.replaceIndex(DATE_TYPE_CODE_PATH,
                dateElementIndex)), "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_DateTypeCode");
        pathValueTracker.add(new Path(Utilities.replaceIndex(DATE_TYPE_CODE_TEXT_PATH,
                dateElementIndex)), dateTypeText);
    }

    private void addModifiedDate(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            return Optional.ofNullable(metacard1.getModifiedDate())
                    .map(Utilities::dateToIso8601);
        }, Utilities.replaceIndex(DATE_PATH, dateElementIndex), this::addModifiedDateExtra);
    }

    private void addModifiedDateExtra(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        addDateTypeCode(pathValueTracker, "lastUpdated", "LastUpdated");
        dateElementIndex++;
    }

    private void addDateStamp(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {

        Optional<Date> modified = Utilities.getOptionalDate(metacard, Core.METACARD_MODIFIED);
        Optional<Date> created = Utilities.getOptionalDate(metacard, Core.METACARD_CREATED);

        Optional<Date> date = modified.isPresent() ?
                modified :
                (created.isPresent() ? created : Optional.empty());

        date.ifPresent(date1 -> pathValueTracker.add(new Path(DATE_TIME_STAMP_PATH),
                Utilities.dateToIso8601(date1)));
    }

    private void addMdIdentificationPointOfContact(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_NAME),
                POINT_OF_CONTACT_NAME_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_PHONE),
                POINT_OF_CONTACT_PHONE_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_ADDRESS),
                POINT_OF_CONTACT_ADDRESS_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.POINT_OF_CONTACT_EMAIL),
                POINT_OF_CONTACT_EMAIL_PATH);

        pathValueTracker.add(new Path(POINT_OF_CONTACT_ROLE_CODE_LIST_PATH),
                "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_RoleCode");
        pathValueTracker.add(new Path(POINT_OF_CONTACT_ROLE_CODE_LIST_VALUE_PATH), "originator");
        pathValueTracker.add(new Path(POINT_OF_CONTACT_ROLE_TEXT_PATH), "Originator");

    }

    private void addContact(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {

        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Contact.PUBLISHER_NAME,
                GMD_CONTACT_ORGANISATION_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.PUBLISHER_PHONE),
                GMD_CONTACT_PHONE_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.PUBLISHER_ADDRESS),
                GMD_POINT_OF_CONTACT_ADDRESS_DELIVERY_POINT_PATH);

        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Contact.PUBLISHER_EMAIL),
                GMD_CONTACT_EMAIL_PATH);

        pathValueTracker.add(new Path(CONTACT_ROLE_CODE_LIST_PATH),
                "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_RoleCode");
        pathValueTracker.add(new Path(CONTACT_ROLE_CODE_LIST_VALUE_PATH), "pointOfContact");
        pathValueTracker.add(new Path(CONTACT_ROLE_TEXT_PATH), "Point of Contact");

    }

    private void addGmdResourceUri(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            return Optional.ofNullable(metacard1.getResourceURI())
                    .filter(Objects::nonNull)
                    .map(URI::toString);
        }, LINKAGE_URI_PATH);
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
                metacard1 -> {
                    return Optional.ofNullable(metacard1.getAttribute(Associations.RELATED))
                            .map(Attribute::getValue)
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .map(this::formatId);
                },
                GMD_ASSOCIATION_PATH,
                this::addMdIdentificationAggregationInfoAggregateDataSetIdentifierExtra);
    }

    private void addMdIdentificationAggregationInfoAggregateDataSetIdentifierExtra(
            XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        pathValueTracker.add(new Path(ASSOCIATIONS_RELATED_CODE_SPACE_PATH), "mediaReferenceNo");
        pathValueTracker.add(new Path(ASSOCIATIONS_RELATED_TYPE_CODE_LIST_PATH),
                "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_AssociationTypeCode");
        pathValueTracker.add(new Path(ASSOCIATIONS_RELATED_TYPE_CODE_LIST_VALUE_PATH),
                "mediaAssociation");
        pathValueTracker.add(new Path(ASSOCIATIONS_RELATED_TYPE_TEXT_PATH), "Media Association");
    }

    private void addMdIdentificationTopicCategories(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Topic.CATEGORY),
                GMD_TOPIC_CATEGORY_PATH);
    }

    private void addMdIdentificationDescriptiveKeywords(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addMultiValues(pathValueTracker,
                Utilities.getValues(metacard, Topic.KEYWORD),
                GMD_KEYWORD_PATH);
    }

    private void addGmdMetacardFormat(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, Media.FORMAT, FORMAT_PATH);
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                Media.FORMAT_VERSION,
                FORMAT_VERSION_PATH);
    }

    private void addMdIdentificationAbstract(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, Core.DESCRIPTION, ABSTRACT_PATH);
    }

    private void addGmdTitle(XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            return Optional.of(StringUtils.defaultString(metacard1.getTitle()));
        }, TITLE_PATH);
    }

    private void addFileIdentifier(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker, metacard, metacard1 -> {
            return Optional.of(StringUtils.defaultString(metacard1.getId()))
                    .map(this::formatId);
        }, FILE_IDENTIFIER_PATH);
    }

    private void addHierarchyLevel(XstreamPathValueTracker pathValueTracker) {
        pathValueTracker.add(new Path(CODE_LIST_VALUE_PATH), "dataset");
        pathValueTracker.add(new Path(CODE_LIST_PATH),
                "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ScopeCode");
    }

    @Override
    protected String getRootNodeName() {
        return GMD_LOCAL_NAME;
    }

    private void addMetadataReleasability(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Attribute releasibilityAttribute = metacard.getAttribute(
                SECURITY_METADATA_RELEASABILITY_ATTRIBUTE);
        Attribute disseminationAttribute = metacard.getAttribute(
                SECURITY_METADATA_DISSEMINATION_ATTRIBUTE);

        if (isReleasabilityAndDisseminationSet(releasibilityAttribute, disseminationAttribute)) {

            String value = disseminationAttribute.getValue()
                    .toString() + " " + Utilities.join(releasibilityAttribute.getValues(), "/");

            pathValueTracker.add(new Path(METADATA_SECURITY_RELEASABILITY_PATH), value);

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

        Attribute releasibilityAttribute = metacard.getAttribute(SECURITY_RESOURCE_RELEASABILITY);
        Attribute disseminationAttribute = metacard.getAttribute(SECURITY_RESOURCE_DISSEMINATION);

        if (isReleasabilityAndDisseminationSet(releasibilityAttribute, disseminationAttribute)) {

            String value = disseminationAttribute.getValue()
                    .toString() + " " + Utilities.join(releasibilityAttribute.getValues(), "/");

            pathValueTracker.add(new Path(RESOURCE_SECURITY_RELEASABILITY_PATH), value);

        }
    }

    private void addResourceSercurityClassification(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                SECURITY_RESOURCE_CLASSIFICATION_ATTRIBUTE,
                RESOURCE_SECURITY_PATH,
                (pathValueTracker1, metacard1) -> {
                    pathValueTracker1.add(new Path(RESOURCE_SECURITY_CODE_LIST_PATH),
                            CLASSIFICATION_CODE);
                });
    }

    private void addMetadataSercurityClassification(XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                SECURITY_METADATA_CLASSIFICATION_ATTRIBUTE,
                METATADA_SECURITY_PATH,
                (pathValueTracker1, metacard1) -> {
                    pathValueTracker1.add(new Path(METATADA_SECURITY_CODE_LIST_PATH),
                            CLASSIFICATION_CODE);
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

        List<Serializable> resourceRatings = Utilities.getSerializables(metacard, RESOURCE_RATING);
        List<Serializable> resourceRatingSystems = Utilities.getSerializables(metacard,
                RESOURCE_RATING_SYSTEM);

        boolean doesResourceRatingsMatch = resourceRatings.size() == resourceRatingSystems.size();

        boolean isResourceRatingsAvailable =
                resourceRatings.size() == 1 && doesResourceRatingsMatch;

        List<Serializable> ratingScaleValues = Utilities.getSerializables(metacard,
                Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE);

        boolean isRatingScaleValuesAvailable = ratingScaleValues.size() == 1;

        if (isCloudCoverageAvailable || isResourceRatingsAvailable
                || isRatingScaleValuesAvailable) {

            List<String> attributeDescription = Stream.of(metacard.getAttribute(Isr.COMMENTS))
                    .flatMap(attribute -> attribute.getValues()
                            .stream())
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());

            if (attributeDescription.size() != 1) {
                pathValueTracker.add(new Path(
                                CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_REASON_PATH),
                        "unknown");
            } else {
                pathValueTracker.add(new Path(
                                CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_DESC_PATH),
                        attributeDescription.get(0));
            }

            pathValueTracker.add(new Path(
                            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_TEXT_PATH),
                    "Image");
            pathValueTracker.add(new Path(
                            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_PATH),
                    "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_CoverageContentTypeCode");
            pathValueTracker.add(new Path(
                            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_VALUE_PATH),
                    "image");
        }

        if (metacard.getAttribute(RESOURCE_RATING) != null && metacard.getAttribute(
                RESOURCE_RATING_SYSTEM) != null &&
                metacard.getAttribute(RESOURCE_RATING)
                        .getValues()
                        .size() == metacard.getAttribute(RESOURCE_RATING_SYSTEM)
                        .getValues()
                        .size()) {

            Utilities.addMultiValues(pathValueTracker,
                    resourceRatings.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList()),
                    RESOURCE_RATING_CODE_PATH);

            Utilities.addMultiValues(pathValueTracker,
                    resourceRatingSystems.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList()),
                    RESOURCE_RATING_CODE_SPACE_PATH);
        } else {

            // TODO needs testing

            if (!ratingScaleValues.isEmpty()) {
                ratingScaleValues.stream()
                        .map(Object::toString)
                        .findFirst()
                        .ifPresent(value -> {
                            pathValueTracker.add(new Path(RESOURCE_RATING_CODE_SPACE_PATH), value);
                            pathValueTracker.add(new Path(RESOURCE_RATING_CODE_PATH), "NIIRS");
                        });
            }

        }

        Utilities.addFieldIfString(pathValueTracker,
                metacard,
                metacard1 -> cloudCoverage,
                CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_PATH);
    }

    private static class Utilities {

        private static List<String> replace(List<String> list, String replacement) {
            return list.stream()
                    .map(value -> replacement)
                    .collect(Collectors.toList());
        }

        private static Optional<Date> getOptionalDate(MetacardImpl metacard, String dateField) {
            return Optional.ofNullable(metacard.getAttribute(dateField))
                    .map(Attribute::getValue)
                    .filter(Objects::nonNull)
                    .filter(Date.class::isInstance)
                    .map(Date.class::cast);
        }

        private static <T> String join(List<T> values, String joiner) {
            return values.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(joiner));
        }

        private static String replaceIndex(String template, int index) {
            return template.replace(INDEX_TAG, Integer.toString(index));
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
            return addFieldIfString(pathValueTracker, metacard, metacard1 -> {
                return Optional.of(metacard1)
                        .map(m -> m.getAttribute(attributeName))
                        .filter(Objects::nonNull)
                        .map(Attribute::getValue)
                        .filter(Objects::nonNull)
                        .filter(String.class::isInstance)
                        .map(String.class::cast);
            }, xpath, extraAdd);
        }

        private static boolean addFieldIfString(XstreamPathValueTracker pathValueTracker,
                MetacardImpl metacard, String attributeName, String xpath) {
            return addFieldIfString(pathValueTracker,
                    metacard,
                    attributeName,
                    xpath,
                    (pathValueTracker1, metacard1) -> {
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
                    (pathValueTracker1, metacard1) -> {
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