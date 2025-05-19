package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

import org.openmrs.Concept;
import org.openmrs.VisitType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BirthAndDeathCohortDefinition;

import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.common.DurationUnit;

import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition.TimeModifier;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;

@Component
public class DRCFacilityBasedARTDeliveryReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		//return inizService.getBooleanFromKey("report.drc.hivStage.active", false);
		return true;
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "f6b09a7e-f330-431d-b7b8-891dd35db0e1";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.facilityBasedARTDelivery.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.drc.facilityBasedARTDelivery.reportDescription");
	}
	
	private Parameter getReportingDateParameter() {
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return new Parameter("onOrBefore", MessageUtil.translate("commonreports.report.util.reportingEndDate"), Date.class,
		        null, DateUtil.parseDate(today, "yyyy-MM-dd"));
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	public static String col3 = "";
	
	public static String col4 = "";
	
	public static String col5 = "";
	
	public static String col6 = "";
	
	public static String col7 = "";
	
	public static String col8 = "";
	
	public static String col9 = "";
	
	public static String col10 = "";
	
	public static String col11 = "";
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getReportingDateParameter());
		
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		
		// facilityBasedARTDelivery Grouping
		CohortCrossTabDataSetDefinition facilityBasedARTDelivery = new CohortCrossTabDataSetDefinition();
		facilityBasedARTDelivery.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(facilityBasedARTDelivery));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrBefore", "${onOrBefore}");
		
		SqlCohortDefinition sqd = new SqlCohortDefinition();
		
		// ART plan -> Started drugs
		// Visit in past 90 days
		// ART refill model -> Normal with visit
		// Not transferred
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_answer.uuid = '1256AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM visit v WHERE v.patient_id = p.patient_id AND v.date_started BETWEEN DATE_SUB(:onOrBefore, INTERVAL 90 DAY) AND :onOrBefore AND v.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM obs o2 JOIN concept cq2 ON o2.concept_id = cq2.concept_id JOIN concept ca2 ON o2.value_coded = ca2.concept_id WHERE o2.person_id = p.patient_id AND cq2.uuid = '166448AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND ca2.uuid = '166447AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o2.voided = 0 AND o2.obs_datetime = (SELECT MAX(o3.obs_datetime) FROM obs o3 WHERE o3.person_id = p.patient_id AND o3.concept_id = cq2.concept_id AND o3.voided = 0)) "
		        + "AND NOT EXISTS (SELECT 1 FROM obs o4 JOIN concept cq4 ON o4.concept_id = cq4.concept_id JOIN concept ca4 ON o4.value_coded = ca4.concept_id WHERE o4.person_id = p.patient_id AND cq4.uuid = '797e0073-1f3f-46b1-8b1a-8cdad134d2b3' AND ca4.uuid = '1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o4.voided = 0 AND o4.obs_datetime = (SELECT MAX(o5.obs_datetime) FROM obs o5 WHERE o5.person_id = p.patient_id AND o5.concept_id = cq4.concept_id AND o5.voided = 0));";
		sqd.setQuery(sql);
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Alive patients
		BirthAndDeathCohortDefinition livePatients = new BirthAndDeathCohortDefinition();
		livePatients.setDied(false);
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(sqd, livePatients);
		facilityBasedARTDelivery.addRow(getName(), ccd, parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		facilityBasedARTDelivery.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		facilityBasedARTDelivery.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		facilityBasedARTDelivery.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityBasedARTDelivery.addColumn(col11, createCohortComposition(_50andAbove), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("commonreports.report.drc.hivStage.males.label");
		col2 = MessageUtil.translate("commonreports.report.drc.hivStage.females.label");
		col3 = MessageUtil.translate("commonreports.report.drc.hivStage.allGenders.label");
		col4 = MessageUtil.translate("commonreports.report.drc.hivStage.belowOneYr.label");
		col5 = MessageUtil.translate("commonreports.report.drc.hivStage.oneToFourYrs.label");
		col6 = MessageUtil.translate("commonreports.report.drc.hivStage.fiveToNineYrs.label");
		col7 = MessageUtil.translate("commonreports.report.drc.hivStage.tenToFourteenYrs.label");
		col8 = MessageUtil.translate("commonreports.report.drc.hivStage.fifteenToNineteenYrs.label");
		col9 = MessageUtil.translate("commonreports.report.drc.hivStage.twentyToTwentyFourYrs.label");
		col10 = MessageUtil.translate("commonreports.report.drc.hivStage.twentyFiveToFourtyNineYrs.label");
		col11 = MessageUtil.translate("commonreports.report.drc.hivStage.fiftyAndAbove.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("637aa0c0-c6ea-435b-a43d-21f01fd8af42", reportDefinition));
	}
}
