package org.openmrs.module.drcreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
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
public class MSPPChildCareReportManager extends ActivatedReportManager {
	
	private String aCol1 = "";
	
	private String aCol2 = "";
	
	private String aCol3 = "";
	
	private String aCol4 = "";
	
	private String aCol5 = "";
	
	private String aCol6 = "";
	
	private String aCol7 = "";
	
	private String aCol8 = "";
	
	private String aCol9 = "";
	
	private String bCol1 = "";
	
	private String bCol2 = "";
	
	private String bCol3 = "";
	
	private String cCol1 = "";
	
	private String cCol2 = "";
	
	private String cCol3 = "";
	
	private String cCol4 = "";
	
	private String cCol5 = "";
	
	private String cCol6 = "";
	
	private String cCol7 = "";
	
	private String cCol8 = "";
	
	private String cCol9 = "";
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Autowired
	@Qualifier("visitService")
	private VisitService vs;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.MSPP.childCare.active", false);
	}
	
	@Override
	public String getUuid() {
		return "bfa63483-d08f-45a3-8997-1147114111e0";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.MSPP.childCare.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.MSPP.childCare.reportDescription");
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition reportDef = new ReportDefinition();
		reportDef.setUuid(getUuid());
		reportDef.setName(getName());
		reportDef.setDescription(getDescription());
		reportDef.setParameters(getParameters());
		
		// category 1
		AgeCohortDefinition under6m = new AgeCohortDefinition();
		under6m.setMinAge(0);
		under6m.setMinAgeUnit(DurationUnit.DAYS);
		under6m.setMaxAge(5);
		under6m.setMaxAgeUnit(DurationUnit.MONTHS);
		under6m.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// category 2
		AgeCohortDefinition _6To23m = new AgeCohortDefinition();
		_6To23m.setMinAge(6);
		_6To23m.setMinAgeUnit(DurationUnit.MONTHS);
		_6To23m.setMaxAge(23);
		_6To23m.setMaxAgeUnit(DurationUnit.MONTHS);
		_6To23m.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// category 3
		AgeCohortDefinition _24To59m = new AgeCohortDefinition();
		_24To59m.setMinAge(24);
		_24To59m.setMinAgeUnit(DurationUnit.MONTHS);
		_24To59m.setMaxAge(59);
		_24To59m.setMaxAgeUnit(DurationUnit.MONTHS);
		_24To59m.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("effectiveDate", "${endDate}");
		
		setColumnNames();
		
		// Creating dataset definitions
		CohortCrossTabDataSetDefinition characteristicsDatasetDefinition = createCharacteristicsDatasetDefinition(under6m,
		    _6To23m, _24To59m, parameterMappings);
		CohortCrossTabDataSetDefinition fateOfChildDatasetDef = createFateOfChildDatasetDefinition(under6m, _6To23m,
		    _24To59m, parameterMappings);
		CohortCrossTabDataSetDefinition vitaminASupplimentationDatasetDef = createVitaminASupplimentationDatasetDefinition(
		    under6m, _6To23m, _24To59m, parameterMappings);
		
		// Adding datasets to the report
		reportDef.addDataSetDefinition(
		    MessageUtil.translate("drcreports.report.MSPP.childCare.characteristics.dataset.name"),
		    Mapped.mapStraightThrough(characteristicsDatasetDefinition));
		reportDef.addDataSetDefinition(MessageUtil.translate("drcreports.report.MSPP.childCare.fateOfChild.dataset.name"),
		    Mapped.mapStraightThrough(fateOfChildDatasetDef));
		reportDef.addDataSetDefinition(
		    MessageUtil.translate("drcreports.report.MSPP.childCare.vitaminASupplimentation.dataset.name"),
		    Mapped.mapStraightThrough(vitaminASupplimentationDatasetDef));
		
		return reportDef;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("0623f477-542c-4b37-8ee6-2a1a4a1821b8", reportDefinition));
	}
	
	private CohortCrossTabDataSetDefinition createCharacteristicsDatasetDefinition(AgeCohortDefinition under6m,
	        AgeCohortDefinition _6To23m, AgeCohortDefinition _24To59m, Map<String, Object> parameterMappings) {
		CohortCrossTabDataSetDefinition characteristicsDatasetDef = new CohortCrossTabDataSetDefinition();
		characteristicsDatasetDef.addParameters(getParameters());
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		
		// Total children seen
		AgeCohortDefinition _0To60m = new AgeCohortDefinition();
		_0To60m.setMinAge(0);
		_0To60m.setMinAgeUnit(DurationUnit.MONTHS);
		_0To60m.setMaxAge(60);
		_0To60m.setMaxAgeUnit(DurationUnit.MONTHS);
		_0To60m.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		VisitCohortDefinition visits = new VisitCohortDefinition();
		visits.setVisitTypeList(vs.getAllVisitTypes(false));
		visits.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
		visits.addParameter(new Parameter("startedOnOrBefore", "On Or Before", Date.class));
		
		Map<String, Object> visitParameterMappings = new HashMap<String, Object>();
		visitParameterMappings.put("startedOnOrAfter", "${startDate}");
		visitParameterMappings.put("startedOnOrBefore", "${endDate}");
		visitParameterMappings.put("effectiveDate", "${endDate}");
		
		CompositionCohortDefinition totalChildrenSeen = createCohortComposition(visits, _0To60m);
		totalChildrenSeen.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// Children seen for the first time
		SqlCohortDefinition childrenSeenFirstTime = new SqlCohortDefinition();
		String sql = "SELECT v.patient_id FROM visit v WHERE v.date_started BETWEEN :onOrAfter AND :onOrBefore "
		        + "AND NOT EXISTS (SELECT 1 FROM visit new_v "
		        + "WHERE new_v.patient_id = v.patient_id AND new_v.visit_id <> v.visit_id);";
		childrenSeenFirstTime.setQuery(sql);
		childrenSeenFirstTime.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		childrenSeenFirstTime.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		childrenSeenFirstTime.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// Children seen for the first time + MUAC measurement
		Concept muacMeasurementConcept = inizService
		        .getConceptFromKey("report.MSPP.childCare.muacMeasurement.numericQuestion.concept");
		
		NumericObsCohortDefinition muacMeasured = new NumericObsCohortDefinition();
		muacMeasured.setQuestion(muacMeasurementConcept);
		muacMeasured.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		muacMeasured.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition childrenMeasuredForMuac = createCohortComposition(childrenSeenFirstTime, muacMeasured);
		childrenMeasuredForMuac.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// Child seen for the first time + weighed / measured
		NumericObsCohortDefinition weightMeasured = new NumericObsCohortDefinition();
		weightMeasured.setQuestion(
		    inizService.getConceptFromKey("report.MSPP.childCare.weightMeasurement.numericQuestion.concept"));
		weightMeasured.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		weightMeasured.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition childrenMeasuredWeight = createCohortComposition(childrenSeenFirstTime, weightMeasured);
		childrenMeasuredWeight.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// Children seen for the first time (115 < MUAC < 125)
		NumericObsCohortDefinition muacMeasuredBetween115And125 = new NumericObsCohortDefinition();
		muacMeasuredBetween115And125.setQuestion(muacMeasurementConcept);
		muacMeasuredBetween115And125.setValue1(11.5);
		muacMeasuredBetween115And125.setOperator1(RangeComparator.GREATER_EQUAL);
		muacMeasuredBetween115And125.setValue2(12.5);
		muacMeasuredBetween115And125.setOperator2(RangeComparator.LESS_EQUAL);
		muacMeasuredBetween115And125.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		muacMeasuredBetween115And125.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition childrenMeasuredForMuacBetween115And125 = createCohortComposition(childrenSeenFirstTime,
		    muacMeasuredBetween115And125);
		childrenMeasuredForMuacBetween115And125.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// Children seen for the first time (MUAC < 115)
		NumericObsCohortDefinition muacMeasuredLessThan115 = new NumericObsCohortDefinition();
		muacMeasuredLessThan115.setQuestion(muacMeasurementConcept);
		muacMeasuredLessThan115.setValue1(11.5);
		muacMeasuredLessThan115.setOperator1(RangeComparator.LESS_THAN);
		muacMeasuredLessThan115.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		muacMeasuredLessThan115.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition childrenMeasuredForMuacLessThan115 = createCohortComposition(childrenSeenFirstTime,
		    muacMeasuredLessThan115);
		childrenMeasuredForMuacLessThan115.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// adding columns
		Map<String, Object> ageParameterMappings = new HashMap<String, Object>();
		ageParameterMappings.put("effectiveDate", "${endDate}");
		characteristicsDatasetDef.addColumn(aCol1, createCohortComposition(males, under6m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol2, createCohortComposition(females, under6m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol3, createCohortComposition(allGenders, under6m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol4, createCohortComposition(males, _6To23m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol5, createCohortComposition(females, _6To23m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol6, createCohortComposition(allGenders, _6To23m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol7, createCohortComposition(males, _24To59m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol8, createCohortComposition(females, _24To59m), ageParameterMappings);
		characteristicsDatasetDef.addColumn(aCol9, createCohortComposition(allGenders, _24To59m), ageParameterMappings);
		
		// adding rows
		characteristicsDatasetDef.addRow(MessageUtil.translate("drcreports.report.MSPP.childCare.total.children.seen"),
		    totalChildrenSeen, visitParameterMappings);
		characteristicsDatasetDef.addRow(
		    MessageUtil.translate("drcreports.report.MSPP.childCare.children.seen.forThe.firstTime"), childrenSeenFirstTime,
		    parameterMappings);
		characteristicsDatasetDef.addRow(MessageUtil.translate("drcreports.report.MSPP.childCare.children.measuredFor.muac"),
		    childrenMeasuredForMuac, parameterMappings);
		characteristicsDatasetDef.addRow(
		    MessageUtil.translate("drcreports.report.MSPP.childCare.children.measuredFor.weight"), childrenMeasuredWeight,
		    parameterMappings);
		characteristicsDatasetDef.addRow(
		    MessageUtil.translate("drcreports.report.MSPP.childCare.children.with.muac.between115And125"),
		    childrenMeasuredForMuacBetween115And125, parameterMappings);
		characteristicsDatasetDef.addRow(MessageUtil.translate("drcreports.report.MSPP.childCare.with.muac.lessThan115"),
		    childrenMeasuredForMuacLessThan115, parameterMappings);
		
		return characteristicsDatasetDef;
	}
	
	private CohortCrossTabDataSetDefinition createFateOfChildDatasetDefinition(AgeCohortDefinition under6m,
	        AgeCohortDefinition _6To23m, AgeCohortDefinition _24To59m, Map<String, Object> parameterMappings) {
		CohortCrossTabDataSetDefinition fateOfChildDatasetDef = new CohortCrossTabDataSetDefinition();
		fateOfChildDatasetDef.addParameters(getParameters());
		
		Concept resultOfVisitQuestion = inizService.getConceptFromKey("report.MSPP.childCare.resultOfVisitQuestion.concept");
		
		VisitType vt = vs
		        .getVisitTypeByUuid(inizService.getValueFromKey("report.MSPP.childCare.malnutrition.visitType.uuid"));
		VisitCohortDefinition malnutritionChildren = new VisitCohortDefinition();
		malnutritionChildren.setVisitTypeList(Arrays.asList(vt));
		
		// admitted
		CodedObsCohortDefinition firstVisitChildren = new CodedObsCohortDefinition();
		firstVisitChildren.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		firstVisitChildren.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		firstVisitChildren.setOperator(SetComparator.IN);
		firstVisitChildren.setQuestion(inizService.getConceptFromKey("report.MSPP.childCare.firstVisitQuestion.concept"));
		firstVisitChildren
		        .setValueList(Arrays.asList(inizService.getConceptFromKey("report.MSPP.childCare.yesAnswer.concept")));
		
		CompositionCohortDefinition childrenOfFirstVisit = createCohortComposition(malnutritionChildren, firstVisitChildren);
		childrenOfFirstVisit.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// cured
		CodedObsCohortDefinition curedChildren = new CodedObsCohortDefinition();
		curedChildren.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		curedChildren.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		curedChildren.setOperator(SetComparator.IN);
		curedChildren.setQuestion(resultOfVisitQuestion);
		Concept curedConcept = inizService.getConceptFromKey("report.MSPP.childCare.resultOfVisit.curedAnswer.concept");
		curedChildren.setValueList(Arrays.asList(curedConcept));
		CompositionCohortDefinition curedMalnutritionChildren = createCohortComposition(malnutritionChildren, curedChildren);
		curedMalnutritionChildren.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// withdrawn
		CodedObsCohortDefinition withdrawnChildren = new CodedObsCohortDefinition();
		withdrawnChildren.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		withdrawnChildren.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		withdrawnChildren.setOperator(SetComparator.IN);
		withdrawnChildren.setQuestion(resultOfVisitQuestion);
		Concept withdrawalConcept = inizService
		        .getConceptFromKey("report.MSPP.childCare.resultOfVisit.withdrawalAnswer.concept");
		withdrawnChildren.setValueList(Arrays.asList(withdrawalConcept));
		CompositionCohortDefinition withdrawnMalnutritionChildren = createCohortComposition(malnutritionChildren,
		    withdrawnChildren);
		withdrawnMalnutritionChildren.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// adding columns
		Map<String, Object> ageParameterMappings = new HashMap<String, Object>();
		ageParameterMappings.put("effectiveDate", "${endDate}");
		fateOfChildDatasetDef.addColumn(bCol1, under6m, ageParameterMappings);
		fateOfChildDatasetDef.addColumn(bCol2, _6To23m, ageParameterMappings);
		fateOfChildDatasetDef.addColumn(bCol3, _24To59m, ageParameterMappings);
		
		// adding rows
		fateOfChildDatasetDef.addRow(MessageUtil.translate("drcreports.report.MSPP.childCare.first.visit.children"),
		    childrenOfFirstVisit, parameterMappings);
		fateOfChildDatasetDef.addRow(curedConcept.getDisplayString(), curedMalnutritionChildren, parameterMappings);
		fateOfChildDatasetDef.addRow(withdrawalConcept.getDisplayString(), withdrawnMalnutritionChildren, parameterMappings);
		
		return fateOfChildDatasetDef;
	}
	
	private CohortCrossTabDataSetDefinition createVitaminASupplimentationDatasetDefinition(AgeCohortDefinition under6m,
	        AgeCohortDefinition _6To23m, AgeCohortDefinition _24To59m, Map<String, Object> parameterMappings) {
		CohortCrossTabDataSetDefinition vitaminASupplimentationDatasetDef = new CohortCrossTabDataSetDefinition();
		vitaminASupplimentationDatasetDef.addParameters(getParameters());
		
		Concept dosageQuestion = inizService.getConceptFromKey("report.MSPP.childCare.dose.numericQuestion.concept");
		
		Concept vaccinationsQuestion = inizService.getConceptFromKey("report.MSPP.childCare.vaccinationsQuestion.concept");
		
		// dose 1
		NumericObsCohortDefinition dose1 = new NumericObsCohortDefinition();
		dose1.setQuestion(dosageQuestion);
		dose1.setValue1(1.0);
		dose1.setOperator1(RangeComparator.EQUAL);
		dose1.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		dose1.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		dose1.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// dose 2
		NumericObsCohortDefinition dose2 = new NumericObsCohortDefinition();
		dose2.setQuestion(dosageQuestion);
		dose2.setValue1(2.0);
		dose2.setOperator1(RangeComparator.EQUAL);
		dose2.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		dose2.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		dose2.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// dose 3
		NumericObsCohortDefinition dose3 = new NumericObsCohortDefinition();
		dose3.setQuestion(dosageQuestion);
		dose3.setValue1(3.0);
		dose3.setOperator1(RangeComparator.EQUAL);
		dose3.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		dose3.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		dose3.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// distribution of Vitamin A
		CodedObsCohortDefinition vitaminA = new CodedObsCohortDefinition();
		vitaminA.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		vitaminA.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		vitaminA.setOperator(SetComparator.IN);
		vitaminA.setQuestion(vaccinationsQuestion);
		Concept vitaminAConcept = inizService.getConceptFromKey("report.MSPP.childCare.vitaminA.concept");
		vitaminA.setValueList(Arrays.asList(vitaminAConcept));
		vitaminA.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// albendazole
		CodedObsCohortDefinition albendazole = new CodedObsCohortDefinition();
		albendazole.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		albendazole.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		albendazole.setOperator(SetComparator.IN);
		albendazole.setQuestion(vaccinationsQuestion);
		Concept albendazoleConcept = inizService.getConceptFromKey("report.MSPP.childCare.albendazole.concept");
		albendazole.setValueList(Arrays.asList(albendazoleConcept));
		albendazole.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		
		// adding columns
		vitaminASupplimentationDatasetDef.addColumn(cCol1, createCohortComposition(under6m, dose1), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol2, createCohortComposition(under6m, dose2), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol3, createCohortComposition(under6m, dose3), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol4, createCohortComposition(_6To23m, dose1), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol5, createCohortComposition(_6To23m, dose2), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol6, createCohortComposition(_6To23m, dose3), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol7, createCohortComposition(_24To59m, dose1), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol8, createCohortComposition(_24To59m, dose2), parameterMappings);
		vitaminASupplimentationDatasetDef.addColumn(cCol9, createCohortComposition(_24To59m, dose3), parameterMappings);
		
		// adding rows
		vitaminASupplimentationDatasetDef.addRow(vitaminAConcept.getDisplayString(), vitaminA, parameterMappings);
		vitaminASupplimentationDatasetDef.addRow(albendazoleConcept.getDisplayString(), albendazole, parameterMappings);
		return vitaminASupplimentationDatasetDef;
	}
	
	private void setColumnNames() {
		aCol1 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory1.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.males.label");
		aCol2 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory1.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.females.label");
		aCol3 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory1.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.total.label");
		aCol4 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory2.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.males.label");
		aCol5 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory2.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.females.label");
		aCol6 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory2.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.total.label");
		aCol7 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory3.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.males.label");
		aCol8 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory3.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.females.label");
		aCol9 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory3.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.total.label");
		
		bCol1 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory1.label");
		bCol2 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory2.label");
		bCol3 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory3.label");
		
		cCol1 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory1.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose1.label");
		cCol2 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory1.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose2.label");
		cCol3 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory1.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose3.label");
		cCol4 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory2.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose1.label");
		cCol5 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory2.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose2.label");
		cCol6 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory2.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose3.label");
		cCol7 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory3.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose1.label");
		cCol8 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory3.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose2.label");
		cCol9 = MessageUtil.translate("drcreports.report.MSPP.childCare.ageCategory3.label") + " - "
		        + MessageUtil.translate("drcreports.report.MSPP.childCare.dose3.label");
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		Long size = Arrays.asList(elements).stream().filter(def -> (def instanceof AgeCohortDefinition)).count();
		if (size > 0) {
			compCD.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		}
		return compCD;
	}
}
