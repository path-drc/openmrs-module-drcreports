package org.openmrs.module.drcreports.reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.api.VisitService;
import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BirthAndDeathCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DRCArtHepatitisReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.drc.artHepatitis.active", true);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "4d824753-e133-4154-ba46-d5af5d3ca66c";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.drc.artHepatitis.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.drc.artHepatitis.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("drcreports.report.util.startDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return new Parameter("endDate", MessageUtil.translate("drcreports.report.util.endDate"), Date.class, null,
		        DateUtil.parseDate(today, "yyyy-MM-dd"));
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	public static String col3 = "";
	
	public static String col4 = "";
	
	public static String col5 = "";
	
	@Autowired
	@Qualifier("visitService")
	private VisitService vs;
	
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
		
		// artHepatitis Grouping
		CohortCrossTabDataSetDefinition artHepatitis = new CohortCrossTabDataSetDefinition();
		artHepatitis.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(artHepatitis));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("startedOnOrAfter", "${startDate}");
		parameterMappings.put("startedOnOrBefore", "${endDate}");
		
		// Current visit in date range
		VisitCohortDefinition visits = new VisitCohortDefinition();
		visits.setVisitTypeList(vs.getAllVisitTypes(false));
		visits.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
		visits.addParameter(new Parameter("startedOnOrBefore", "On Or Before", Date.class));
		
		SqlCohortDefinition ptLivingWithHIVsqd = new SqlCohortDefinition();
		//What do you want to do? ---> Enrol a new client, Transfer in a client, Enrol a Mother into PMTCT program, Re-enrol a client
		String ptLivingWithHIVsql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '83e40f2c-c316-43e6-a12e-20a338100281' AND c_answer.uuid IN ('164144AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','160563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','163532AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA','159833AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') AND o.voided = 0);";
		ptLivingWithHIVsqd.setQuery(ptLivingWithHIVsql);
		
		SqlCohortDefinition ptStartedOnARTsqd = new SqlCohortDefinition();
		// ART plan -> Started drugs
		String ptStartedOnARTsql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_answer.uuid = '1256AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0);";
		ptStartedOnARTsqd.setQuery(ptStartedOnARTsql);
		
		// Live patients
		BirthAndDeathCohortDefinition livePatients = new BirthAndDeathCohortDefinition();
		livePatients.setDied(false);
		
		SqlCohortDefinition ptWithHepatitisBOrdersSqd = new SqlCohortDefinition();
		// Hepatitis B Surface Antigen Test Order in date range
		String ptWithHepatitisBOrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid = '159430AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore);";
		ptWithHepatitisBOrdersSqd.setQuery(ptWithHepatitisBOrdersSql);
		ptWithHepatitisBOrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithHepatitisBOrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(visits, ptLivingWithHIVsqd, ptStartedOnARTsqd, livePatients, ptWithHepatitisBOrdersSqd);
		
		SqlCohortDefinition ptWithPositiveHepatitisBOrdersSqd = new SqlCohortDefinition();
		// Hepatitis B Surface Antigen Test Order in date range
		// Reult is Positive
		String ptWithPositiveHepatitisBOrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid = '159430AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore "
		        + "AND EXISTS (SELECT 1 FROM obs obs JOIN concept obs_concept ON obs.concept_id = obs_concept.concept_id JOIN concept value_concept ON obs.value_coded = value_concept.concept_id WHERE obs.order_id = o.order_id AND obs_concept.uuid = '159430AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND value_concept.uuid = '703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND obs.voided = 0));";
		ptWithPositiveHepatitisBOrdersSqd.setQuery(ptWithPositiveHepatitisBOrdersSql);
		ptWithPositiveHepatitisBOrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithPositiveHepatitisBOrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(visits, ptLivingWithHIVsqd, ptStartedOnARTsqd, livePatients,
		    ptWithPositiveHepatitisBOrdersSqd);
		
		SqlCohortDefinition ptWithHepatitisCOrdersSqd = new SqlCohortDefinition();
		// Hepatitis C Surface Antigen Test Order in date range
		String ptWithHepatitisCOrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid = '1325AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore);";
		ptWithHepatitisCOrdersSqd.setQuery(ptWithHepatitisCOrdersSql);
		ptWithHepatitisCOrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithHepatitisCOrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(visits, ptLivingWithHIVsqd, ptStartedOnARTsqd, livePatients, ptWithHepatitisCOrdersSqd);
		
		SqlCohortDefinition ptWithPositiveHepatitisCOrdersSqd = new SqlCohortDefinition();
		// Hepatitis C Surface Antigen Test Order in date range
		// Reult is Positive
		String ptWithPositiveHepatitisCOrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid = '1325AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore "
		        + "AND EXISTS (SELECT 1 FROM obs obs JOIN concept obs_concept ON obs.concept_id = obs_concept.concept_id JOIN concept value_concept ON obs.value_coded = value_concept.concept_id WHERE obs.order_id = o.order_id AND obs_concept.uuid = '1325AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND value_concept.uuid = '703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND obs.voided = 0));";
		ptWithPositiveHepatitisCOrdersSqd.setQuery(ptWithPositiveHepatitisCOrdersSql);
		ptWithPositiveHepatitisCOrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithPositiveHepatitisCOrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd4 = new CompositionCohortDefinition();
		ccd4.initializeFromElements(visits, ptLivingWithHIVsqd, ptStartedOnARTsqd, livePatients,
		    ptWithPositiveHepatitisCOrdersSqd);
		
		//Add Rows
		artHepatitis.addRow(MessageUtil.translate("drcreports.report.drc.ptWithHepatitisBOrders"), ccd, parameterMappings);
		artHepatitis.addRow(MessageUtil.translate("drcreports.report.drc.ptWithPositiveHepatitisBOrders"), ccd2,
		    parameterMappings);
		artHepatitis.addRow(MessageUtil.translate("drcreports.report.drc.ptWithHepatitisCOrders"), ccd3, parameterMappings);
		artHepatitis.addRow(MessageUtil.translate("drcreports.report.drc.ptWithPositiveHepatitisCOrders"), ccd4,
		    parameterMappings);
		
		//Add Columns
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		artHepatitis.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		artHepatitis.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		artHepatitis.addColumn(col3, createCohortComposition(allGenders), null);
		
		// 0-14 years
		AgeCohortDefinition _0To14y = new AgeCohortDefinition();
		_0To14y.setMinAge(0);
		_0To14y.setMinAgeUnit(DurationUnit.DAYS);
		_0To14y.setMaxAge(14);
		_0To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_0To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artHepatitis.addColumn(col4, createCohortComposition(_0To14y), null);
		
		// 15+ years
		AgeCohortDefinition _15andAbove = new AgeCohortDefinition();
		_15andAbove.setMinAge(15);
		_15andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_15andAbove.setMaxAge(200);
		_15andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_15andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artHepatitis.addColumn(col5, createCohortComposition(_15andAbove), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("drcreports.report.drc.males.label");
		col2 = MessageUtil.translate("drcreports.report.drc.females.label");
		col3 = MessageUtil.translate("drcreports.report.drc.allGenders.label");
		col4 = MessageUtil.translate("drcreports.report.drc.belowFifteenYrs.label");
		col5 = MessageUtil.translate("drcreports.report.drc.fifteenAndAbove.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("0a4b31cf-bc98-4378-b585-6169a1a30053", reportDefinition));
	}
}
