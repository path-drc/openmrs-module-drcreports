package org.openmrs.module.drcreports.reports;

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
import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.drcreports.DRCReportsConstants;
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
public class DRCFacilityDispensationReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		//return inizService.getBooleanFromKey("report.drc.active", false);
		return true;
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "4253d3b2-f108-4672-a2a6-b7eb67a51a10";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.drc.facilityDispensation.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.drc.facilityDispensation.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("drcreports.report.util.reportingStartDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", MessageUtil.translate("drcreports.report.util.reportingEndDate"), Date.class);
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
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		
		// facilityDispensation Grouping
		CohortCrossTabDataSetDefinition facilityDispensation = new CohortCrossTabDataSetDefinition();
		facilityDispensation.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(facilityDispensation));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		// ART refill for <90 days(< 3 months) with reporting in range
		SqlCohortDefinition sqd = new SqlCohortDefinition();
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o_num JOIN concept c_num ON o_num.concept_id = c_num.concept_id WHERE o_num.person_id = p.patient_id AND c_num.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989' AND o_num.voided = 0 AND o_num.value_numeric < 90 AND o_num.obs_datetime BETWEEN :onOrAfter AND :onOrBefore); ";
		sqd.setQuery(sql);
		sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(sqd);
		
		// ART refill for 90-149 days(3-5 months) with reporting in range
		SqlCohortDefinition sqd2 = new SqlCohortDefinition();
		String sql2 = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o_num JOIN concept c_num ON o_num.concept_id = c_num.concept_id "
		        + "WHERE o_num.person_id = p.patient_id " + "AND c_num.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989' "
		        + "AND o_num.voided = 0 " + "AND o_num.value_numeric >= 90 AND o_num.value_numeric <= 149 "
		        + "AND o_num.obs_datetime BETWEEN :onOrAfter AND :onOrBefore); ";
		
		sqd2.setQuery(sql2);
		sqd2.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd2.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(sqd2);
		
		// ART refill for >=150 days(>5months) with reporting in range
		SqlCohortDefinition sqd3 = new SqlCohortDefinition();
		String sql3 = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o_num JOIN concept c_num ON o_num.concept_id = c_num.concept_id "
		        + "WHERE o_num.person_id = p.patient_id " + "AND c_num.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989' "
		        + "AND o_num.voided = 0 " + "AND o_num.value_numeric >= 150 "
		        + "AND o_num.obs_datetime BETWEEN :onOrAfter AND :onOrBefore); ";
		
		sqd3.setQuery(sql3);
		sqd3.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd3.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(sqd3);
		
		facilityDispensation.addRow(MessageUtil.translate("drcreports.report.drc.facilityDispensationBelow3months"), ccd,
		    parameterMappings);
		facilityDispensation.addRow(MessageUtil.translate("drcreports.report.drc.facilityDispensation3To5months"), ccd2,
		    parameterMappings);
		facilityDispensation.addRow(MessageUtil.translate("drcreports.report.drc.facilityDispensationAbove5months"), ccd3,
		    parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		facilityDispensation.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		facilityDispensation.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		facilityDispensation.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		facilityDispensation.addColumn(col11, createCohortComposition(_50andAbove), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("drcreports.report.drc.males.label");
		col2 = MessageUtil.translate("drcreports.report.drc.females.label");
		col3 = MessageUtil.translate("drcreports.report.drc.allGenders.label");
		col4 = MessageUtil.translate("drcreports.report.drc.belowOneYr.label");
		col5 = MessageUtil.translate("drcreports.report.drc.oneToFourYrs.label");
		col6 = MessageUtil.translate("drcreports.report.drc.fiveToNineYrs.label");
		col7 = MessageUtil.translate("drcreports.report.drc.tenToFourteenYrs.label");
		col8 = MessageUtil.translate("drcreports.report.drc.fifteenToNineteenYrs.label");
		col9 = MessageUtil.translate("drcreports.report.drc.twentyToTwentyFourYrs.label");
		col10 = MessageUtil.translate("drcreports.report.drc.twentyFiveToFourtyNineYrs.label");
		col11 = MessageUtil.translate("drcreports.report.drc.fiftyAndAbove.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("c68114a8-5a61-4e58-a07d-e69f75cde5a6", reportDefinition));
	}
}
