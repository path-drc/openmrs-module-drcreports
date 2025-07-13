package org.openmrs.module.drcreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
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

@Component(DRCReportsConstants.COMPONENT_REPORTMANAGER_ANTENATAL)
public class MSPPAntenatalReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.MSPP.antenatal.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "b6b2a422-38d1-4fb1-acfc-46eb842bcf7f";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.MSPP.antenatal.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.MSPP.antenatal.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	public String getGestationName() {
		return MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.reportName");
	}
	
	public String getRisksName() {
		return MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.reportName");
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	public static String col3 = "";
	
	public static String col4 = "";
	
	public static String col5 = "";
	
	public static String col6 = "";
	
	public static String risksCol1 = "";
	
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
		
		// Antenatal general
		CohortCrossTabDataSetDefinition antenatalGestation = new CohortCrossTabDataSetDefinition();
		antenatalGestation.addParameters(getParameters());
		rd.addDataSetDefinition(getGestationName(), Mapped.mapStraightThrough(antenatalGestation));
		
		// Risks
		CohortCrossTabDataSetDefinition antenatalRisks = new CohortCrossTabDataSetDefinition();
		antenatalRisks.addParameters(getParameters());
		rd.addDataSetDefinition(getRisksName(), Mapped.mapStraightThrough(antenatalRisks));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		String[] gestationDurations = inizService.getValueFromKey("report.MSPP.antenatal.gestationDuration").split(",");
		for (String member : gestationDurations) {
			
			if (member.equals("Total")) {
				CodedObsCohortDefinition gestationDuration = new CodedObsCohortDefinition();
				gestationDuration.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				gestationDuration.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				gestationDuration.setOperator(SetComparator.IN);
				gestationDuration
				        .setGroupingConcept(inizService.getConceptFromKey("report.MSPP.antenatal.estimatedGestationalAge"));
				gestationDuration.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.numberOfWeeks"));
				
				antenatalGestation.addRow(member, gestationDuration, parameterMappings);
			} else {
				
				String[] bit = member.split("-");
				String firstNumber = bit[0];
				String lastNumber = bit[1];
				
				NumericObsCohortDefinition gestationDuration = new NumericObsCohortDefinition();
				gestationDuration
				        .setGroupingConcept(inizService.getConceptFromKey("report.MSPP.antenatal.estimatedGestationalAge"));
				gestationDuration.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.numberOfWeeks"));
				gestationDuration.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				gestationDuration.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				gestationDuration.setValue1(Double.parseDouble(firstNumber));
				gestationDuration.setValue2(Double.parseDouble(lastNumber));
				gestationDuration.setOperator1(RangeComparator.GREATER_EQUAL);
				gestationDuration.setOperator2(RangeComparator.LESS_EQUAL);
				
				antenatalGestation.addRow(
				    member + " " + MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.gestationWeeks"),
				    gestationDuration, parameterMappings);
				
			}
			
		}
		
		// Risks related
		GenderCohortDefinition female = new GenderCohortDefinition();
		female.setFemaleIncluded(true);
		
		// Risky Pregnancies
		CodedObsCohortDefinition riskyPregnancy = new CodedObsCohortDefinition();
		riskyPregnancy.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		riskyPregnancy.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		riskyPregnancy.setOperator(SetComparator.IN);
		
		riskyPregnancy.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.riskyPregnancy"));
		riskyPregnancy.setValueList(Arrays.asList(inizService.getConceptFromKey("report.MSPP.antenatal.yes")));
		antenatalRisks.addRow(MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.riskyPregnancy"), riskyPregnancy,
		    parameterMappings);
		
		// Iron def ANC visit Pregnancies
		CodedObsCohortDefinition anemia = new CodedObsCohortDefinition();
		anemia.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		anemia.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		anemia.setOperator(SetComparator.IN);
		anemia.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.codedDiagnosis"));
		anemia.setValueList(Arrays.asList(inizService.getConceptFromKey("report.MSPP.antenatal.anemiaIronDeficiency")));
		
		VisitCohortDefinition _prenatal = new VisitCohortDefinition();
		_prenatal.setVisitTypeList(Arrays.asList(Context.getVisitService()
		        .getVisitTypeByUuid(inizService.getValueFromKey("report.MSPP.antenatal.prenatalVisitType"))));
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(_prenatal, anemia, female);
		antenatalRisks.addRow(MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.ironDefANC"), ccd,
		    parameterMappings);
		
		// Prenatal visit + Fer Folate Co prescribed
		SqlCohortDefinition sqd = new SqlCohortDefinition("select patient_id from orders where concept_id="
		        + inizService.getConceptFromKey("report.MSPP.antenatal.ferrousFolate").getConceptId()
		        + " and order_type_id =(select order_type_id from order_type where uuid = '"
		        + inizService.getValueFromKey("report.MSPP.antenatal.drugOrder")
		        + "') AND date_created BETWEEN :onOrAfter AND :onOrBefore");
		
		sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd1 = new CompositionCohortDefinition();
		ccd1.initializeFromElements(_prenatal, sqd, female);
		antenatalRisks.addRow(MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.prenatalIron"), ccd1,
		    parameterMappings);
		
		// Prenatal visit treated for Fe def (Same as above--> Prenatal visit + Fer
		// Folate Co prescribed)
		antenatalRisks.addRow(MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.prenatalIronDef"), ccd1,
		    parameterMappings);
		
		// Mothers with a birth plan
		//Assumption made that who ever comes for visit has a birth plan
		CodedObsCohortDefinition birthPlan = new CodedObsCohortDefinition();
		birthPlan.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		birthPlan.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		birthPlan.setOperator(SetComparator.IN);
		
		birthPlan.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.visitNumber"));
		
		List<Concept> visitNumbers = new ArrayList<Concept>();
		visitNumbers.add(inizService.getConceptFromKey("report.MSPP.antenatal.one"));
		visitNumbers.add(inizService.getConceptFromKey("report.MSPP.antenatal.two"));
		visitNumbers.add(inizService.getConceptFromKey("report.MSPP.antenatal.three"));
		visitNumbers.add(inizService.getConceptFromKey("report.MSPP.antenatal.four"));
		visitNumbers.add(inizService.getConceptFromKey("report.MSPP.antenatal.fivePlus"));
		
		birthPlan.setValueList(visitNumbers);
		antenatalRisks.addRow(MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.motherBirthPlan"), birthPlan,
		    parameterMappings);
		
		// Prenatal visit + malaria test positive + chloroquine co prescribed
		CodedObsCohortDefinition malaria = new CodedObsCohortDefinition();
		malaria.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		malaria.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		malaria.setOperator(SetComparator.IN);
		
		malaria.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.malaria"));
		
		List<Concept> malariaPositiveConceptSet = new ArrayList<Concept>();
		malariaPositiveConceptSet.add(inizService.getConceptFromKey("report.MSPP.antenatal.positive"));
		malariaPositiveConceptSet.add(inizService.getConceptFromKey("report.MSPP.antenatal.onePlus"));
		malariaPositiveConceptSet.add(inizService.getConceptFromKey("report.MSPP.antenatal.twoPlus"));
		malariaPositiveConceptSet.add(inizService.getConceptFromKey("report.MSPP.antenatal.threePlus"));
		malariaPositiveConceptSet.add(inizService.getConceptFromKey("report.MSPP.antenatal.fourPlus"));
		
		malaria.setValueList(malariaPositiveConceptSet);
		
		SqlCohortDefinition sql = new SqlCohortDefinition("select patient_id from orders where concept_id="
		        + inizService.getConceptFromKey("report.MSPP.antenatal.chloroquine").getConceptId()
		        + " and order_type_id =(select order_type_id from order_type where uuid = '"
		        + inizService.getValueFromKey("report.MSPP.antenatal.drugOrder")
		        + "') AND date_created BETWEEN :onOrAfter AND :onOrBefore");
		sql.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sql.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(_prenatal, sql, malaria, female);
		antenatalRisks.addRow(
		    MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.prenatalMalariaPositiveChloroquine"), ccd2,
		    parameterMappings);
		
		// Prenatal + MUAC =<21cm
		NumericObsCohortDefinition muac = new NumericObsCohortDefinition();
		muac.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		muac.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		muac.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.midUpperArmCircumference"));
		muac.setOperator1(RangeComparator.GREATER_EQUAL);
		muac.setValue1(0.0);
		muac.setOperator2(RangeComparator.LESS_EQUAL);
		muac.setValue2(21.0);
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(_prenatal, muac, female);
		antenatalRisks.addRow(MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.prenatalMUAC=<21cm"), ccd3,
		    parameterMappings);
		
		// Women + fer folate co prescribed
		VisitService vs = Context.getVisitService();
		List<VisitType> lVT = new ArrayList<VisitType>();
		String[] otherVisitTypes = inizService.getValueFromKey("report.MSPP.antenatal.otherVisitTypes").split(",");
		
		for (String member : otherVisitTypes) {
			lVT.add(vs.getVisitTypeByUuid(member));
		}
		
		VisitCohortDefinition _other = new VisitCohortDefinition();
		_other.setVisitTypeList(lVT);
		
		SqlCohortDefinition sqdc = new SqlCohortDefinition("select patient_id from orders where concept_id="
		        + inizService.getConceptFromKey("report.MSPP.antenatal.ferrousFolate").getConceptId()
		        + " and order_type_id =(select order_type_id from order_type where uuid = '"
		        + inizService.getValueFromKey("report.MSPP.antenatal.drugOrder")
		        + "') AND date_created BETWEEN :onOrAfter AND :onOrBefore");
		sqdc.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqdc.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd5 = new CompositionCohortDefinition();
		ccd5.initializeFromElements(_other, sqdc, female);
		antenatalRisks.addRow(MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.womenIron"), ccd5,
		    parameterMappings);
		
		setColumnNames();
		
		// First Visit column
		CodedObsCohortDefinition firstVisit = new CodedObsCohortDefinition();
		firstVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		firstVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		firstVisit.setOperator(SetComparator.IN);
		
		firstVisit.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.visitNumber"));
		firstVisit.addValue(inizService.getConceptFromKey("report.MSPP.antenatal.one"));
		antenatalGestation.addColumn(col1, createCohortComposition(firstVisit), null);
		
		// Second Visit column
		CodedObsCohortDefinition secondVisit = new CodedObsCohortDefinition();
		secondVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		secondVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		secondVisit.setOperator(SetComparator.IN);
		secondVisit.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.visitNumber"));
		secondVisit.addValue(inizService.getConceptFromKey("report.MSPP.antenatal.two"));
		antenatalGestation.addColumn(col2, createCohortComposition(secondVisit), null);
		
		// Third Visit column
		CodedObsCohortDefinition thirdVisit = new CodedObsCohortDefinition();
		thirdVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		thirdVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		thirdVisit.setOperator(SetComparator.IN);
		thirdVisit.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.visitNumber"));
		thirdVisit.addValue(inizService.getConceptFromKey("report.MSPP.antenatal.three"));
		antenatalGestation.addColumn(col3, createCohortComposition(thirdVisit), null);
		
		// Fourth Visit column
		CodedObsCohortDefinition fourthVisit = new CodedObsCohortDefinition();
		fourthVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		fourthVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		fourthVisit.setOperator(SetComparator.IN);
		fourthVisit.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.visitNumber"));
		fourthVisit.addValue(inizService.getConceptFromKey("report.MSPP.antenatal.four"));
		antenatalGestation.addColumn(col4, createCohortComposition(fourthVisit), null);
		
		// Fifth Visit column
		CodedObsCohortDefinition fifthVisit = new CodedObsCohortDefinition();
		fifthVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		fifthVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		fifthVisit.setOperator(SetComparator.IN);
		fifthVisit.setQuestion(inizService.getConceptFromKey("report.MSPP.antenatal.visitNumber"));
		fifthVisit.addValue(inizService.getConceptFromKey("report.MSPP.antenatal.fivePlus"));
		antenatalGestation.addColumn(col5, createCohortComposition(fifthVisit), null);
		
		// Total Visit column
		CodedObsCohortDefinition totalVisit = new CodedObsCohortDefinition();
		antenatalGestation.addColumn(col6, createCohortComposition(totalVisit), null);
		
		// Risks related
		// All columns
		GenderCohortDefinition allGender = new GenderCohortDefinition();
		allGender.setMaleIncluded(true);
		allGender.setFemaleIncluded(true);
		antenatalRisks.addColumn(risksCol1, createCohortComposition(allGender), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.firstVisit.label");
		col2 = MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.secondVisit.label");
		col3 = MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.thirdVisit.label");
		col4 = MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.fourthVisit.label");
		col5 = MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.fifthVisit.label");
		col6 = MessageUtil.translate("drcreports.report.MSPP.antenatalGestation.total.label");
		risksCol1 = MessageUtil.translate("drcreports.report.MSPP.antenatalRisks.all.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("a0ebe3c1-d6a3-4761-bff0-6a03626d2c75", reportDefinition));
	}
}
