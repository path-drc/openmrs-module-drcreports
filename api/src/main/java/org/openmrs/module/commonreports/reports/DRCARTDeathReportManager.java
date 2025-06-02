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
public class DRCARTDeathReportManager extends ActivatedReportManager {
	
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
		return "537763bb-a4d2-4e77-81d9-cae2588227ea";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.artDeath.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.drc.artDeath.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("commonreports.report.util.reportingStartDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", MessageUtil.translate("commonreports.report.util.reportingEndDate"), Date.class);
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
		
		// artDeath Grouping
		CohortCrossTabDataSetDefinition artDeath = new CohortCrossTabDataSetDefinition();
		artDeath.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(artDeath));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		SqlCohortDefinition sqd = new SqlCohortDefinition();
		
		// Death Date in range
		// ART Start/Initiation before death date
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o_date JOIN concept c_date ON o_date.concept_id = c_date.concept_id WHERE o_date.person_id = p.patient_id AND c_date.uuid = '1543AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o_date.voided = 0 AND o_date.value_datetime BETWEEN :onOrAfter AND :onOrBefore AND o_date.obs_datetime = (SELECT MAX(od_inner.obs_datetime) FROM obs od_inner WHERE od_inner.person_id = p.patient_id AND od_inner.concept_id = c_date.concept_id AND od_inner.voided = 0)) "
		        + "AND EXISTS (SELECT 1 FROM obs o_earlier JOIN concept c_earlier ON o_earlier.concept_id = c_earlier.concept_id WHERE o_earlier.person_id = p.patient_id AND c_earlier.uuid = '159599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o_earlier.voided = 0 AND o_earlier.value_datetime < (SELECT latest_date_obs.value_datetime FROM obs latest_date_obs JOIN concept latest_date_concept ON latest_date_obs.concept_id = latest_date_concept.concept_id WHERE latest_date_obs.person_id = p.patient_id AND latest_date_concept.uuid = '1543AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND latest_date_obs.voided = 0 AND latest_date_obs.obs_datetime = (SELECT MAX(ld_inner.obs_datetime) FROM obs ld_inner WHERE ld_inner.person_id = p.patient_id AND ld_inner.concept_id = latest_date_concept.concept_id AND ld_inner.voided = 0)));";
		sqd.setQuery(sql);
		sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Dead patients
		BirthAndDeathCohortDefinition deadPatients = new BirthAndDeathCohortDefinition();
		deadPatients.setDied(true);
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(sqd, deadPatients);
		artDeath.addRow(getName(), ccd, parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		artDeath.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		artDeath.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		artDeath.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artDeath.addColumn(col11, createCohortComposition(_50andAbove), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("commonreports.report.drc.males.label");
		col2 = MessageUtil.translate("commonreports.report.drc.females.label");
		col3 = MessageUtil.translate("commonreports.report.drc.allGenders.label");
		col4 = MessageUtil.translate("commonreports.report.drc.belowOneYr.label");
		col5 = MessageUtil.translate("commonreports.report.drc.oneToFourYrs.label");
		col6 = MessageUtil.translate("commonreports.report.drc.fiveToNineYrs.label");
		col7 = MessageUtil.translate("commonreports.report.drc.tenToFourteenYrs.label");
		col8 = MessageUtil.translate("commonreports.report.drc.fifteenToNineteenYrs.label");
		col9 = MessageUtil.translate("commonreports.report.drc.twentyToTwentyFourYrs.label");
		col10 = MessageUtil.translate("commonreports.report.drc.twentyFiveToFourtyNineYrs.label");
		col11 = MessageUtil.translate("commonreports.report.drc.fiftyAndAbove.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("b26dc58c-9cdc-405a-8ba6-1e6462e4d66f", reportDefinition));
	}
}
