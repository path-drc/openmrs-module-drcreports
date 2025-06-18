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
public class DRCBiologicalMonitoringReportManager extends ActivatedReportManager {
	
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
		return "51df052e-8c8b-4c70-8fa2-509a82d2dced";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.biologicalMonitoring.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.drc.biologicalMonitoring.reportDescription");
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
	
	public static String col8 = "";
	
	public static String col9 = "";
	
	public static String col10 = "";
	
	public static String col11 = "";
	
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
		
		// biologicalMonitoring Grouping
		CohortCrossTabDataSetDefinition biologicalMonitoring = new CohortCrossTabDataSetDefinition();
		biologicalMonitoring.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(biologicalMonitoring));
		
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
		//What do you want to do? ---> Enroll new Pt in HIV Care
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '83e40f2c-c316-43e6-a12e-20a338100281' AND c_answer.uuid = '164144AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0);";
		ptLivingWithHIVsqd.setQuery(sql);
		
		SqlCohortDefinition ptWithCd4OrdersSqd = new SqlCohortDefinition();
		// CD4 count Order in date range
		String ptWithCd4OrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid = '5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore);";
		ptWithCd4OrdersSqd.setQuery(ptWithCd4OrdersSql);
		ptWithCd4OrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithCd4OrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Alive patients
		BirthAndDeathCohortDefinition livePatients = new BirthAndDeathCohortDefinition();
		livePatients.setDied(false);
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithCd4OrdersSqd);
		
		ConceptService cs = Context.getConceptService();
		// CD4 count less than 200
		NumericObsCohortDefinition cd4CountLessThan200 = new NumericObsCohortDefinition();
		cd4CountLessThan200.setQuestion(cs.getConceptByUuid("5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		cd4CountLessThan200.setValue1(200.0);
		cd4CountLessThan200.setOperator1(RangeComparator.LESS_THAN);
		cd4CountLessThan200.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		cd4CountLessThan200.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithCd4OrdersSqd, cd4CountLessThan200);
		
		// CD4 count greater than 200
		NumericObsCohortDefinition cd4CountGreaterThan200 = new NumericObsCohortDefinition();
		cd4CountGreaterThan200.setQuestion(cs.getConceptByUuid("5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		cd4CountGreaterThan200.setValue1(200.0);
		cd4CountGreaterThan200.setOperator1(RangeComparator.GREATER_THAN);
		cd4CountGreaterThan200.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		cd4CountGreaterThan200.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		SqlCohortDefinition ptWithTBLamOrdersSqd = new SqlCohortDefinition();
		// TB LAM Order in date range
		String ptWithTBLamOrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid = '167459AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore);";
		ptWithTBLamOrdersSqd.setQuery(ptWithTBLamOrdersSql);
		ptWithTBLamOrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithTBLamOrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithCd4OrdersSqd, ptWithTBLamOrdersSqd,
		    cd4CountGreaterThan200);
		
		SqlCohortDefinition ptWithCrAgOrdersSqd = new SqlCohortDefinition();
		// Serum CrAg Order in date range
		String ptWithCrAgOrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid IN ('3726102f-b161-409d-a71d-a8ced7909380', '163613AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore);";
		ptWithCrAgOrdersSqd.setQuery(ptWithCrAgOrdersSql);
		ptWithCrAgOrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithCrAgOrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd4 = new CompositionCohortDefinition();
		ccd4.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithCd4OrdersSqd, ptWithCrAgOrdersSqd,
		    cd4CountLessThan200);
		
		SqlCohortDefinition ptWithVLOrdersSqd = new SqlCohortDefinition();
		// VL Order in date range
		String ptWithVLOrdersSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM orders o JOIN concept c ON o.concept_id = c.concept_id WHERE o.patient_id = p.patient_id AND c.uuid = '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o.date_activated BETWEEN :onOrAfter AND :onOrBefore);";
		ptWithVLOrdersSqd.setQuery(ptWithVLOrdersSql);
		ptWithVLOrdersSqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		ptWithVLOrdersSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		SqlCohortDefinition artStartDate6MonthsAgoSqd = new SqlCohortDefinition();
		
		// ART Start date 180 (6 months) +/- 15 days (165-195 days)
		String artStartDate6MonthsAgoSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c ON o.concept_id = c.concept_id JOIN concept c_value ON o.value_coded = c_value.concept_id "
		        + "JOIN obs o_date ON o_date.person_id = o.person_id AND DATE(o.obs_datetime) = DATE(o_date.obs_datetime) "
		        + "JOIN concept c_date ON o_date.concept_id = c_date.concept_id "
		        + "WHERE o.person_id = p.patient_id AND c.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_value.uuid = '1256AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' "
		        + "AND c_date.uuid = '159599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o_date.voided = 0 "
		        + "AND o_date.value_datetime BETWEEN DATE_SUB(:onOrBefore, INTERVAL 195 DAY) AND DATE_SUB(:onOrBefore, INTERVAL 165 DAY));";
		artStartDate6MonthsAgoSqd.setQuery(artStartDate6MonthsAgoSql);
		artStartDate6MonthsAgoSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd5 = new CompositionCohortDefinition();
		ccd5.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate6MonthsAgoSqd);
		
		// VL ≤ 50
		NumericObsCohortDefinition vlLessEqual50 = new NumericObsCohortDefinition();
		vlLessEqual50.setQuestion(cs.getConceptByUuid("856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		vlLessEqual50.setValue1(50.0);
		vlLessEqual50.setOperator1(RangeComparator.LESS_EQUAL);
		vlLessEqual50.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		vlLessEqual50.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// VL  > 50 and ≤ 1000
		NumericObsCohortDefinition vlgreaterthan50lessEqual1000 = new NumericObsCohortDefinition();
		vlgreaterthan50lessEqual1000.setQuestion(cs.getConceptByUuid("856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		vlgreaterthan50lessEqual1000.setValue1(50.0);
		vlgreaterthan50lessEqual1000.setValue2(1000.0);
		vlgreaterthan50lessEqual1000.setOperator1(RangeComparator.GREATER_THAN);
		vlgreaterthan50lessEqual1000.setOperator2(RangeComparator.LESS_EQUAL);
		vlgreaterthan50lessEqual1000.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		vlgreaterthan50lessEqual1000.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// VL  > 1000
		NumericObsCohortDefinition vlGreaterthan1000 = new NumericObsCohortDefinition();
		vlGreaterthan1000.setQuestion(cs.getConceptByUuid("856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		vlGreaterthan1000.setValue1(1000.0);
		vlGreaterthan1000.setOperator1(RangeComparator.GREATER_THAN);
		vlGreaterthan1000.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		vlGreaterthan1000.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd6 = new CompositionCohortDefinition();
		ccd6.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate6MonthsAgoSqd,
		    vlLessEqual50);
		
		CompositionCohortDefinition ccd7 = new CompositionCohortDefinition();
		ccd7.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate6MonthsAgoSqd,
		    vlgreaterthan50lessEqual1000);
		
		CompositionCohortDefinition ccd8 = new CompositionCohortDefinition();
		ccd8.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate6MonthsAgoSqd,
		    vlGreaterthan1000);
		
		SqlCohortDefinition artStartDate12MonthsAgoSqd = new SqlCohortDefinition();
		
		// ART Start date 365 days (12 months) +/- 15 days (350-380 days)
		String artStartDate12MonthsAgoSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c ON o.concept_id = c.concept_id JOIN concept c_value ON o.value_coded = c_value.concept_id "
		        + "JOIN obs o_date ON o_date.person_id = o.person_id AND DATE(o.obs_datetime) = DATE(o_date.obs_datetime) "
		        + "JOIN concept c_date ON o_date.concept_id = c_date.concept_id "
		        + "WHERE o.person_id = p.patient_id AND c.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_value.uuid = '1256AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' "
		        + "AND c_date.uuid = '159599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o_date.voided = 0 "
		        + "AND o_date.value_datetime BETWEEN DATE_SUB(:onOrBefore, INTERVAL 380 DAY) AND DATE_SUB(:onOrBefore, INTERVAL 350 DAY));";
		artStartDate12MonthsAgoSqd.setQuery(artStartDate12MonthsAgoSql);
		artStartDate12MonthsAgoSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd9 = new CompositionCohortDefinition();
		ccd9.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate12MonthsAgoSqd);
		
		CompositionCohortDefinition ccd10 = new CompositionCohortDefinition();
		ccd10.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate12MonthsAgoSqd,
		    vlLessEqual50);
		
		CompositionCohortDefinition ccd11 = new CompositionCohortDefinition();
		ccd11.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate12MonthsAgoSqd,
		    vlgreaterthan50lessEqual1000);
		
		CompositionCohortDefinition ccd12 = new CompositionCohortDefinition();
		ccd12.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd, artStartDate12MonthsAgoSqd,
		    vlGreaterthan1000);
		
		SqlCohortDefinition artStartDateGT12MonthsAgoSqd = new SqlCohortDefinition();
		
		// ART Start date > 365 days (12 months) + 15 days (380 days)
		String artStartDateGT12MonthsAgoSql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c ON o.concept_id = c.concept_id JOIN concept c_value ON o.value_coded = c_value.concept_id "
		        + "JOIN obs o_date ON o_date.person_id = o.person_id AND DATE(o.obs_datetime) = DATE(o_date.obs_datetime) "
		        + "JOIN concept c_date ON o_date.concept_id = c_date.concept_id "
		        + "WHERE o.person_id = p.patient_id AND c.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_value.uuid = '1256AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' "
		        + "AND c_date.uuid = '159599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0 AND o_date.voided = 0 "
		        + "AND o_date.value_datetime < DATE_SUB(:onOrBefore, INTERVAL 380 DAY));";
		artStartDateGT12MonthsAgoSqd.setQuery(artStartDateGT12MonthsAgoSql);
		artStartDateGT12MonthsAgoSqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd13 = new CompositionCohortDefinition();
		ccd13.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd,
		    artStartDateGT12MonthsAgoSqd);
		
		CompositionCohortDefinition ccd14 = new CompositionCohortDefinition();
		ccd14.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd,
		    artStartDateGT12MonthsAgoSqd, vlLessEqual50);
		
		CompositionCohortDefinition ccd15 = new CompositionCohortDefinition();
		ccd15.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd,
		    artStartDateGT12MonthsAgoSqd, vlgreaterthan50lessEqual1000);
		
		CompositionCohortDefinition ccd16 = new CompositionCohortDefinition();
		ccd16.initializeFromElements(visits, ptLivingWithHIVsqd, livePatients, ptWithVLOrdersSqd,
		    artStartDateGT12MonthsAgoSqd, vlGreaterthan1000);
		
		//Add Rows
		biologicalMonitoring.addRow(MessageUtil.translate("commonreports.report.drc.ptWithCd4Orders"), ccd,
		    parameterMappings);
		biologicalMonitoring.addRow(MessageUtil.translate("commonreports.report.drc.ptWithCd4OrdersWithCD4CountBelow200"),
		    ccd2, parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptWithCd4OrdersWithCD4CountAbove200AndTBLam"), ccd3,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptWithCd4OrdersWithCD4CountBelow200AndCrAg"), ccd4,
		    parameterMappings);
		biologicalMonitoring.addRow(MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt6Months"), ccd5,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt6MonthsLessEqual50"), ccd6,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt6MonthsGreater50LessEqual1000"), ccd7,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt6MonthsGreaterthan1000"), ccd8,
		    parameterMappings);
		biologicalMonitoring.addRow(MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt12Months"), ccd9,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt12MonthsLessEqual50"), ccd10,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt12MonthsGreater50LessEqual1000"), ccd11,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLAt12MonthsGreaterthan1000"), ccd12,
		    parameterMappings);
		biologicalMonitoring.addRow(MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLGT12Months"), ccd13,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLGT12MonthsLessEqual50"), ccd14,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLGT12MonthsGreater50LessEqual1000"), ccd15,
		    parameterMappings);
		biologicalMonitoring.addRow(
		    MessageUtil.translate("commonreports.report.drc.ptStartedOnArtWithVLGT12MonthsGreaterthan1000"), ccd16,
		    parameterMappings);
		//Add Columns
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		biologicalMonitoring.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		biologicalMonitoring.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		biologicalMonitoring.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		biologicalMonitoring.addColumn(col11, createCohortComposition(_50andAbove), null);
		
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
		        .asList(ReportManagerUtil.createCsvReportDesign("93ff3eb3-6365-420e-ac31-06e203acaf10", reportDefinition));
	}
}
