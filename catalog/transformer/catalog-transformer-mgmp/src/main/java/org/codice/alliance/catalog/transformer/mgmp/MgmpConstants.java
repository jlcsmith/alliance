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
 */
package org.codice.alliance.catalog.transformer.mgmp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;

public final class MgmpConstants {

    public static final String GML_NAMESPACE_PATH = "/MD_Metadata/@xmlns:gml";

    public static final String XLINK_NAMESPACE_PATH = "/MD_Metadata/@xmlns:xlink";

    public static final String GML_NAMESPACE = "http://www.opengis.net/gml/3.2";

    public static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";

    public static final String INDEX_TAG = "%index%";

    public static final String MGMP_CHARACTER_SET_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_CharacterSetCode";

    public static final String ENCODING_TYPE = "utf8";

    public static final String ENCODING_DESCRIPTION = "UTF-8";

    public static final String MGMP_ASSOCIATION_TYPE_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_AssociationTypeCode";

    public static final String MGMP_SCOPE_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ScopeCode";

    public static final String MGMP_COVERAGE_CONTENT_TYPE_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_CoverageContentTypeCode";

    public static final String MD_METADATA_STANDARD_VERSION_PATH =
            "/MD_Metadata/metadataStandardVersion/gco:CharacterString";

    public static final String MGMP_CLASSIFICATION_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_ClassificationCode";

    public static final String MD_METADATA_STANDARD_NAME_PATH =
            "/MD_Metadata/metadataStandardName/gco:CharacterString";

    public static final String MGMP_ROLE_CODE =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_RoleCode";

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

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_TEXT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode";

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_CONTENT_TYPE_CODE_LIST_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeList";

    public static final String
            CONTENT_INFO_MGMP_IMAGE_DESCRIPTION_CLOUD_COVERAGE_ATTRIBUTE_REASON_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/attributeDescription/@gco:nilReason";

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

    public static final String LANGUAGE_CODE_LIST =
            "http://mod.uk/spatial/codelist/mgmp/2.0/MGMP_LanguageCode";

    public static final String GMD_METACARD_TYPE_NAME = "gmd.MD_Metadata";

    public static final String GMD_NAMESPACE = "http://www.isotc211.org/2005/gmd";

    public static final String GCO_NAMESPACE = "http://www.isotc211.org/2005/gco";

    public static final String POINT_OF_CONTACT_ROLE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode/@codeList";

    public static final String POINT_OF_CONTACT_ROLE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode/@codeListValue";

    public static final String POINT_OF_CONTACT_ROLE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/pointOfContact/CI_ResponsibleParty/role/CI_RoleCode";

    public static final String RESOURCE_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeList";

    public static final String RESOURCE_ORIGINATOR_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeList";

    public static final String RESOURCE_STATUS_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/status/MD_ProgressCode/@codeList";

    public static final String GMD_MIN_ALTITUDE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/minimumValue/gco:Real";

    public static final String GMD_VERTICAL_CRS_XLINK_HREF_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/@xlink:href";

    public static final String GMD_VERTICAL_CRS_XLINK_TITLE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/@xlink:title";

    public static final String ASSOCIATIONS_RELATED_CODE_SPACE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/aggregateDataSetIdentifier/RS_Identifier/codeSpace/gco:CharacterString";

    public static final String ASSOCIATIONS_RELATED_TYPE_CODE_LIST_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode/@codeList";

    public static final String ASSOCIATIONS_RELATED_TYPE_CODE_LIST_VALUE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode/@codeListValue";

    public static final String ASSOCIATIONS_RELATED_TYPE_TEXT_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/aggregationInfo/MD_AggregateInformation/associationType/DS_AssociationTypeCode";

    public static final String METADATA_SECURITY_CODE_LIST_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeList";

    public static final String METADATA_ORIGINATOR_CLASSIFICATION_CODE_LIST_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeList";

    public static final String GMD_POLYGON_SRSNAME = "http://www.opengis.net/def/crs/EPSG/0/4326";

    public static final String GMD_NAMESPACE_PATH = "/MD_Metadata/@xmlns";

    public static final String GCO_NAMESPACE_PATH =
            "/MD_Metadata/@xmlns:" + GmdConstants.GCO_PREFIX;

    public static final String MGMP_NAMESPACE_PATH = "/MD_Metadata/@xmlns:mgmp";

    public static final String MGMP_NAMESPACE = "http://mod.uk/spatial/ns/mgmp/2.0";

    public static final String LANGUAGE_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/language/LanguageCode/@codeListValue";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_NUMBER_PATH =
            "/MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/code/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_NUMBER_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:identifier/mgmp:MGMP_CRS_Identifier/codeSpace/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:grid/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:projection/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:ellipsoid/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:datum/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    public static final String MGMP_SPATIAL_REFERENCE_SYSTEM_CRS_WKT_TYPE_PATH =
            "/MD_Metadata/referenceSystemInfo/mgmp:MGMP_ReferenceSystem/mgmp:crs/mgmp:MGMP_CRS_Object/mgmp:specification/mgmp:MGMP_CRS_WKTSpecification/mgmp:wkt/gco:CharacterString";

    /* MD_CoverageDescription */
    public static final String ISR_COVERAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/MD_CoverageDescription/attributeDescription/gco:RecordType";

    public static final String ISR_COVERAGE_CATEGORY_PATH =
            "/MD_Metadata/contentInfo/MD_CoverageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    /* MGMP_VideoDescription */
    public static final String ISR_VIDEO_COMMENT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_VideoDescription/attributeDescription/gco:RecordType";

    public static final String ISR_VIDEO_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_VideoDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    /* MGMP_ImageDescription */
    public static final String ISR_IMAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/attributeDescription/gco:RecordType";

    public static final String ISR_IMAGE_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    public static final String ISR_MD_IMAGE_COMMENT_PATH =
            "/MD_Metadata/contentInfo/MD_ImageDescription/attributeDescription/gco:RecordType";

    public static final String ISR_MD_IMAGE_DESCRIPTION_PATH =
            "/MD_Metadata/contentInfo/MD_ImageDescription/contentType/MD_CoverageContentTypeCode/@codeListValue";

    public static final String NIIRS_RATING_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode/RS_Identifier/code/gco:CharacterString";

    public static final String CLOUD_COVERAGE_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/cloudCoverPercentage/gco:Real";

    public static final String NIIRS_PATH =
            "/MD_Metadata/contentInfo/mgmp:MGMP_ImageDescription/imageQualityCode/RS_Identifier/codeSpace/gco:CharacterString";

    public static final String METADATA_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    public static final String METADATA_RELEASABILITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    public static final String METADATA_ORIGINATOR_SECURITY_PATH =
            "/MD_Metadata/metadataConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_SECURITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/classification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_ORIGINATOR_SECURITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:originatorClassification/MD_ClassificationCode/@codeListValue";

    public static final String RESOURCE_SECURITY_RELEASABILITY_PATH =
            "/MD_Metadata/identificationInfo/MD_DataIdentification/resourceConstraints/mgmp:MGMP_SecurityConstraints/mgmp:caveat/gco:CharacterString";

    public static final List<String> DATA_QUALITY_LIST = Arrays.asList(
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/MGMP_UsabilityElement",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_GriddedDataPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy",
            "/MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_AbsoluteExternalPositionalAccuracy");

    public static final String FORMAT_PATH =
            "/MD_Metadata/distributionInfo/MD_Distribution/distributionFormat/MD_Format/name/mgmp:MGMP_FormatCode/@codeListValue";

    public static final String DQ_QUANTITATIVE_RESULT = "DQ_QuantitativeResult";

    public static final String DESCRIPTIVE_RESULT = "mgmp:MGMP_DescriptiveResult";

    /* Defaults  */

    public static final List<String> DEFAULT_LANGUAGE =
            Collections.singletonList(Locale.ENGLISH.getISO3Country()
                    .toLowerCase());

    /* Misc */

    public static final String NIIRS = "NIIRS";

    public static final String EYES_ONLY = "Eyes Only";

    public static final String EYES_DISCRETION = "Eyes Discretion";

    public static final String RELEASABLE_TO = "Releasable to";

    public static final String RESULT = "result";

    public static final String NAME_OF_MEASURE = "nameOfMeasure";

    private MgmpConstants() {

    }
}