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
public class DRCARTFirstLineFifteenYearsAndAboveReportManager extends ActivatedReportManager {
	
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
		return "a7e2f4f1-a976-47c6-a05c-e65b9bec32f3";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.artFirstLineFifteenYearsAndAbove.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.drc.artFirstLineFifteenYearsAndAbove.reportDescription");
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
		
		// artFirstLineFifteenYearsAndAbove Grouping
		CohortCrossTabDataSetDefinition artFirstLineFifteenYearsAndAbove = new CohortCrossTabDataSetDefinition();
		artFirstLineFifteenYearsAndAbove.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(artFirstLineFifteenYearsAndAbove));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		SqlCohortDefinition sqd = new SqlCohortDefinition();
		
		// Visit in range
		// ART regimen -> ABC/3TC+DTG
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
		// ART regimen -> TDF+3TC+DTG
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
		// ART regimen -> TDF/3TC/EFV
		//Exact combination doesn't exist. Not added as a row for now.
		String sql3 = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM visit v WHERE v.patient_id = p.patient_id AND v.date_started BETWEEN :onOrAfter AND :onOrBefore AND v.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '164432AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_answer.uuid = '0da33711-7b3f-4997-bc9e-4a2445fbaf63' AND o.voided = 0   AND o.obs_datetime = (SELECT MAX(do_inner.obs_datetime) FROM obs do_inner WHERE do_inner.person_id = p.patient_id AND do_inner.concept_id = c_question.concept_id AND do_inner.voided = 0) );";
		sqd3.setQuery(sql3);
		sqd3.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd3.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(sqd3, livePatients);
		
		artFirstLineFifteenYearsAndAbove.addRow(MessageUtil.translate("commonreports.report.drc.ABC+3TC+pDTG"), ccd,
		    parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		artFirstLineFifteenYearsAndAbove.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		artFirstLineFifteenYearsAndAbove.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		artFirstLineFifteenYearsAndAbove.addColumn(col3, createCohortComposition(allGenders), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineFifteenYearsAndAbove.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineFifteenYearsAndAbove.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineFifteenYearsAndAbove.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artFirstLineFifteenYearsAndAbove.addColumn(col11, createCohortComposition(_50andAbove), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("commonreports.report.drc.males.label");
		col2 = MessageUtil.translate("commonreports.report.drc.females.label");
		col3 = MessageUtil.translate("commonreports.report.drc.allGenders.label");
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
		        .asList(ReportManagerUtil.createCsvReportDesign("adb7b241-f712-4009-a895-9cd91e76e299", reportDefinition));
	}
}
