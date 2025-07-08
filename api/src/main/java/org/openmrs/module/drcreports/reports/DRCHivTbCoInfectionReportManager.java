package org.openmrs.module.drcreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BirthAndDeathCohortDefinition;

import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
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

@Component
public class DRCHivTbCoInfectionReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.drc.hivTbCoInfection.active", true);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "887abe18-4203-4f1e-a7f9-a809f2a4fc63";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.drc.hivTBCoInfection.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.drc.hivTBCoInfection.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("drcreports.report.util.reportingStartDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", MessageUtil.translate("drcreports.report.util.reportingEndDate"), Date.class);
	}
	
	public String getHivTBCoInfectionName() {
		return MessageUtil.translate("drcreports.report.drc.hivTBCoInfection.reportName");
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
		
		// hivTBCoInfection Grouping
		CohortCrossTabDataSetDefinition hivTBCoInfection = new CohortCrossTabDataSetDefinition();
		hivTBCoInfection.addParameters(getParameters());
		rd.addDataSetDefinition(getHivTBCoInfectionName(), Mapped.mapStraightThrough(hivTBCoInfection));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("startedOnOrAfter", "${startDate}");
		parameterMappings.put("startedOnOrBefore", "${endDate}");
		
		Map<String, Object> parameterMappings2 = new HashMap<String, Object>();
		parameterMappings2.put("onOrAfter", "${startDate}");
		parameterMappings2.put("onOrBefore", "${endDate}");
		
		VisitCohortDefinition visits = new VisitCohortDefinition();
		visits.setVisitTypeList(vs.getAllVisitTypes(false));
		visits.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
		visits.addParameter(new Parameter("startedOnOrBefore", "On Or Before", Date.class));
		
		ConceptService cs = Context.getConceptService();
		
		// Newly Enrolled HIV Pt
		CodedObsCohortDefinition newHIVEnrollment = new CodedObsCohortDefinition();
		newHIVEnrollment.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		newHIVEnrollment.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		newHIVEnrollment.setOperator(SetComparator.IN);
		newHIVEnrollment.setQuestion(cs.getConceptByUuid("83e40f2c-c316-43e6-a12e-20a338100281")); //What do you want to do?
		newHIVEnrollment.setTimeModifier(TimeModifier.LAST);
		List<Concept> newHIVEnrollmentAnswers = new ArrayList<Concept>();
		newHIVEnrollmentAnswers.add(cs.getConceptByUuid("164144AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Enroll new Pt in HIV Care
		newHIVEnrollment.setValueList(newHIVEnrollmentAnswers);
		
		//TB Screening
		CodedObsCohortDefinition tBScreening = new CodedObsCohortDefinition();
		tBScreening.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		tBScreening.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		tBScreening.setOperator(SetComparator.IN);
		tBScreening.setQuestion(cs.getConceptByUuid("f8868467-bd15-4576-9da8-bfb8ef64ea17")); //TB Screening
		tBScreening.setTimeModifier(TimeModifier.LAST);
		List<Concept> tBScreeningAnswer = new ArrayList<Concept>();
		tBScreeningAnswer.add(cs.getConceptByUuid("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //Yes
		tBScreening.setValueList(tBScreeningAnswer);
		
		// Alive patients
		BirthAndDeathCohortDefinition livePatients = new BirthAndDeathCohortDefinition();
		livePatients.setDied(false);
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(visits, newHIVEnrollment, tBScreening, livePatients);
		// Presumptive TB signs
		CodedObsCohortDefinition presumptiveTBSigns = new CodedObsCohortDefinition();
		presumptiveTBSigns.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		presumptiveTBSigns.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		presumptiveTBSigns.setOperator(SetComparator.IN);
		presumptiveTBSigns.setQuestion(cs.getConceptByUuid("12a22a0b-f0ed-4f1a-8d70-7c6acda5ae78")); //What do you want to do?
		presumptiveTBSigns.setTimeModifier(TimeModifier.LAST);
		List<Concept> presumptiveTBSignsAnswers = new ArrayList<Concept>();
		
		presumptiveTBSignsAnswers.add(cs.getConceptByUuid("133027AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Night Sweats
		presumptiveTBSignsAnswers.add(cs.getConceptByUuid("1494AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Fever lasting more than three weeks
		presumptiveTBSignsAnswers.add(cs.getConceptByUuid("159799AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // cough lasting more than 2 weeks
		presumptiveTBSignsAnswers.add(cs.getConceptByUuid("1858AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Weight Loss (Abnormal weight loss)
		
		presumptiveTBSigns.setValueList(presumptiveTBSignsAnswers);
		
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(visits, presumptiveTBSigns, livePatients);
		
		SqlCohortDefinition ptLivingWithHIVsqd = new SqlCohortDefinition();
		
		//What do you want to do? ---> Enroll new Pt in HIV Care
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '83e40f2c-c316-43e6-a12e-20a338100281' AND c_answer.uuid = '164144AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0);";
		ptLivingWithHIVsqd.setQuery(sql);
		
		//Action taken - Presumptive TB ----> GeneXpert MTB/Rif Ordered
		CodedObsCohortDefinition tBTestDoneGeneExpert = new CodedObsCohortDefinition();
		tBTestDoneGeneExpert.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		tBTestDoneGeneExpert.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		tBTestDoneGeneExpert.setOperator(SetComparator.IN);
		tBTestDoneGeneExpert.setQuestion(cs.getConceptByUuid("a39dfa34-a139-4335-9eac-219d6fedf868")); //Action taken - Presumptive TB
		tBTestDoneGeneExpert.setTimeModifier(TimeModifier.LAST);
		List<Concept> tBTestDoneGeneExpertAnswer = new ArrayList<Concept>();
		tBTestDoneGeneExpertAnswer.add(cs.getConceptByUuid("164945AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //GeneXpert MTB/Rif Ordered
		tBTestDoneGeneExpert.setValueList(tBTestDoneGeneExpertAnswer);
		
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(ptLivingWithHIVsqd, presumptiveTBSigns, tBTestDoneGeneExpert, livePatients);
		
		//Action taken - Presumptive TB ----> Ziehl Ordered
		CodedObsCohortDefinition tBTestDoneZN = new CodedObsCohortDefinition();
		tBTestDoneZN.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		tBTestDoneZN.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		tBTestDoneZN.setOperator(SetComparator.IN);
		tBTestDoneZN.setQuestion(cs.getConceptByUuid("a39dfa34-a139-4335-9eac-219d6fedf868")); //Action taken - Presumptive TB
		tBTestDoneZN.setTimeModifier(TimeModifier.LAST);
		List<Concept> tBTestDoneZNAnswer = new ArrayList<Concept>();
		tBTestDoneZNAnswer.add(cs.getConceptByUuid("307AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //Ziehl Ordered
		tBTestDoneZN.setValueList(tBTestDoneZNAnswer);
		
		CompositionCohortDefinition ccd4 = new CompositionCohortDefinition();
		ccd4.initializeFromElements(ptLivingWithHIVsqd, presumptiveTBSigns, tBTestDoneZN, livePatients);
		
		//Clinically dx with TB
		CodedObsCohortDefinition tbClinicalDx = new CodedObsCohortDefinition();
		tbClinicalDx.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		tbClinicalDx.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		tbClinicalDx.setOperator(SetComparator.IN);
		tbClinicalDx.setQuestion(cs.getConceptByUuid("160108AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //TB screening outcome
		tbClinicalDx.setTimeModifier(TimeModifier.LAST);
		List<Concept> tbClinicalDxAnswer = new ArrayList<Concept>();
		tbClinicalDxAnswer.add(cs.getConceptByUuid("703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //Positive (Presumptive TB)
		tbClinicalDx.setValueList(tbClinicalDxAnswer);
		CompositionCohortDefinition ccd5 = new CompositionCohortDefinition();
		ccd5.initializeFromElements(ptLivingWithHIVsqd, presumptiveTBSigns, tbClinicalDx, livePatients);
		
		//Evaluated for TB prophylaxis
		CodedObsCohortDefinition evalTBProphylaxis = new CodedObsCohortDefinition();
		evalTBProphylaxis.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		evalTBProphylaxis.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		evalTBProphylaxis.setOperator(SetComparator.IN);
		evalTBProphylaxis.setQuestion(cs.getConceptByUuid("162275AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Evaluated for tuberculosis prophylaxis
		evalTBProphylaxis.setTimeModifier(TimeModifier.LAST);
		List<Concept> evalTBProphylaxisAnswer = new ArrayList<Concept>();
		evalTBProphylaxisAnswer.add(cs.getConceptByUuid("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Yes
		evalTBProphylaxis.setValueList(evalTBProphylaxisAnswer);
		
		//Date tuberculosis prophylaxis started
		DateObsCohortDefinition tbProphylaxisDate = new DateObsCohortDefinition();
		tbProphylaxisDate.setTimeModifier(TimeModifier.LAST);
		tbProphylaxisDate.setQuestion(cs.getConceptByUuid("162320AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Date tuberculosis prophylaxis started
		tbProphylaxisDate.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
		tbProphylaxisDate.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
		CompositionCohortDefinition ccd6 = new CompositionCohortDefinition();
		ccd6.initializeFromElements(newHIVEnrollment, evalTBProphylaxis, tbProphylaxisDate, livePatients);
		
		//Completed TB Prophylaxis
		CodedObsCohortDefinition completedTBProphylaxis = new CodedObsCohortDefinition();
		completedTBProphylaxis.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		completedTBProphylaxis.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		completedTBProphylaxis.setOperator(SetComparator.IN);
		completedTBProphylaxis.setQuestion(cs.getConceptByUuid("166463AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //Previously completed Tuberculosis preventive treatment
		completedTBProphylaxis.setTimeModifier(TimeModifier.LAST);
		List<Concept> completedTBProphylaxisAnswer = new ArrayList<Concept>();
		completedTBProphylaxisAnswer.add(cs.getConceptByUuid("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //Yes
		completedTBProphylaxis.setValueList(completedTBProphylaxisAnswer);
		CompositionCohortDefinition ccd7 = new CompositionCohortDefinition();
		ccd7.initializeFromElements(ptLivingWithHIVsqd, completedTBProphylaxis, livePatients);
		
		hivTBCoInfection.addRow(MessageUtil.translate("drcreports.report.drc.newHivTBCoInfection"), ccd, parameterMappings);
		hivTBCoInfection.addRow(MessageUtil.translate("drcreports.report.drc.presumptiveTBSigns"), ccd2, parameterMappings);
		hivTBCoInfection.addRow(MessageUtil.translate("drcreports.report.drc.tBTestDoneGeneExpert"), ccd3, null);
		
		hivTBCoInfection.addRow(MessageUtil.translate("drcreports.report.drc.tBTestDoneZiehl"), ccd4, parameterMappings2);
		hivTBCoInfection.addRow(MessageUtil.translate("drcreports.report.drc.tbClinicalDx"), ccd5, parameterMappings2);
		hivTBCoInfection.addRow(MessageUtil.translate("drcreports.report.drc.newHIVStartedTBProphylaxis"), ccd6,
		    parameterMappings2);
		hivTBCoInfection.addRow(MessageUtil.translate("drcreports.report.drc.ptLivingWithHIVTBProphylaxisCompleted"), ccd7,
		    parameterMappings2);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		hivTBCoInfection.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		hivTBCoInfection.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		hivTBCoInfection.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivTBCoInfection.addColumn(col11, createCohortComposition(_50andAbove), null);
		
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
		        .asList(ReportManagerUtil.createCsvReportDesign("df39d6aa-f2bf-42f1-89a5-55f5a82e964b", reportDefinition));
	}
}
