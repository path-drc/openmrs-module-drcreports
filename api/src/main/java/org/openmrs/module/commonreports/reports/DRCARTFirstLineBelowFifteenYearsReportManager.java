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
public class DRCARTFirstLineBelowFifteenYearsReportManager extends ActivatedReportManager {
	
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
		return "121d040d-a420-468f-83f2-bb5c119d2dd3";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.artFirstLineBelowFifteenYears.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.drc.artFirstLineBelowFifteenYears.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("commonreports.report.util.startDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return new Parameter("endDate", MessageUtil.translate("commonreports.report.util.endDate"), Date.class, null,
		        DateUtil.parseDate(today, "yyyy-MM-dd"));
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	public static String col3 = "";
	
	public static String col4 = "";
	
	public static String col5 = "";
	
	public static String col6 = "";
	
	public static String col7 = "";
	
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
		
		// artFirstLineBelowFifteenYears Grouping
		CohortCrossTabDataSetDefinition artFirstLineBelowFifteenYears = new CohortCrossTabDataSetDefinition();
		artFirstLineBelowFifteenYears.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(artFirstLineBelowFifteenYears));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		SqlCohortDefinition sqd = new SqlCohortDefinition();
		
		// Visit in range
		// ART regimen -> ABC/3TC+pDTG (< 30kg)
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM visit v WHERE v.patient_id = p.patient_id AND v.date_started BETWEEN :onOrAfter AND :onOrBefore AND v.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '164432AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_answer.uuid = '159db6bc-1303-4829-8fe4-32212751c934' AND o.voided = 0   AND o.obs_datetime = (SELECT MAX(do_inner.obs_datetime) FROM obs do_inner WHERE do_inner.person_id = p.patient_id AND do_inner.concept_id = c_question.concept_id AND do_inner.voided = 0) );";
		sqd.setQuery(sql);
		sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Alive patients
		BirthAndDeathCohortDefinition livePatients = new BirthAndDeathCohortDefinition();
		livePatients.setDied(false);
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(sqd, livePatients);
		
		SqlCohortDefinition sqd2 = new SqlCohortDefinition();
		
		// Visit in range
		// ART regimen -> TDF+3TC+DTG (≥30kg)
		//Exact combination doesn't exist. Not added as a row for now.
		String sql2 = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM visit v WHERE v.patient_id = p.patient_id AND v.date_started BETWEEN :onOrAfter AND :onOrBefore AND v.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '164432AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_answer.uuid = '159db6bc-1303-4829-8fe4-32212751c934' AND o.voided = 0   AND o.obs_datetime = (SELECT MAX(do_inner.obs_datetime) FROM obs do_inner WHERE do_inner.person_id = p.patient_id AND do_inner.concept_id = c_question.concept_id AND do_inner.voided = 0) );";
		sqd2.setQuery(sql2);
		sqd2.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd2.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(sqd2, livePatients);
		
		SqlCohortDefinition sqd3 = new SqlCohortDefinition();
		
		// Visit in range
		// ART regimen -> ABC/3TC+LPV/pellet
		String sql3 = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM visit v WHERE v.patient_id = p.patient_id AND v.date_started BETWEEN :onOrAfter AND :onOrBefore AND v.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '164432AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_answer.uuid = '0da33711-7b3f-4997-bc9e-4a2445fbaf63' AND o.voided = 0   AND o.obs_datetime = (SELECT MAX(do_inner.obs_datetime) FROM obs do_inner WHERE do_inner.person_id = p.patient_id AND do_inner.concept_id = c_question.concept_id AND do_inner.voided = 0) );";
		sqd3.setQuery(sql3);
		sqd3.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd3.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(sqd3, livePatients);
		
		SqlCohortDefinition sqd4 = new SqlCohortDefinition();
		
		// Visit in range
		// ART regimen -> ABC/3TC+LPV/r 125 mg
		// Currently no differetiation with the one above. Combining this into one row for now
		String sql4 = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM visit v WHERE v.patient_id = p.patient_id AND v.date_started BETWEEN :onOrAfter AND :onOrBefore AND v.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '164432AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_answer.uuid = '0da33711-7b3f-4997-bc9e-4a2445fbaf63' AND o.voided = 0   AND o.obs_datetime = (SELECT MAX(do_inner.obs_datetime) FROM obs do_inner WHERE do_inner.person_id = p.patient_id AND do_inner.concept_id = c_question.concept_id AND do_inner.voided = 0) );";
		sqd4.setQuery(sql4);
		sqd4.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd4.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd4 = new CompositionCohortDefinition();
		ccd4.initializeFromElements(sqd4, livePatients);
		
		artFirstLineBelowFifteenYears.addRow(MessageUtil.translate("commonreports.report.drc.ABC+3TC+pDTG"), ccd,
		    parameterMappings);
		artFirstLineBelowFifteenYears.addRow(MessageUtil.translate("commonreports.report.drc.ABC+3TC+LPV"), ccd3,
		    parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		artFirstLineBelowFifteenYears.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		artFirstLineBelowFifteenYears.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		artFirstLineBelowFifteenYears.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineBelowFifteenYears.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineBelowFifteenYears.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineBelowFifteenYears.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineBelowFifteenYears.addColumn(col7, createCohortComposition(_10To14y), null);
		
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
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("a38b4f6f-cf0f-45fc-bba0-edf9761417db", reportDefinition));
	}
}
