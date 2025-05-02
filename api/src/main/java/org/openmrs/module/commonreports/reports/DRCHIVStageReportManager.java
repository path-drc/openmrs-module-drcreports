package org.openmrs.module.commonreports.reports;

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
public class DRCHIVStageReportManager extends ActivatedReportManager {
	
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
		return "27b977d2-02bf-4ef9-b512-9b9c495962b8";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.hivStage.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.drc.hivStage.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	private Parameter getDefaultDateParameter() {
		return new Parameter("defaultDateTime", "Default Obs Date", Date.class, null,
		        DateUtil.parseDate("1970-01-01", "yyyy-MM-dd"));
	}
	
	public String getHivStage3And4Name() {
		return MessageUtil.translate("commonreports.report.drc.hivStage.reportName");
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
		params.add(getDefaultDateParameter());
		
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		
		// hivStage3And4 Grouping
		CohortCrossTabDataSetDefinition hivStage3And4 = new CohortCrossTabDataSetDefinition();
		hivStage3And4.addParameters(getParameters());
		rd.addDataSetDefinition(getHivStage3And4Name(), Mapped.mapStraightThrough(hivStage3And4));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		//parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrAfter", "${defaultDateTime}");
		
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("startedOnOrAfter", "${startDate}");
		parameterMappings.put("startedOnOrBefore", "${endDate}");
		
		VisitCohortDefinition visits = new VisitCohortDefinition();
		visits.setVisitTypeList(vs.getAllVisitTypes(false));
		visits.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
		visits.addParameter(new Parameter("startedOnOrBefore", "On Or Before", Date.class));
		
		ConceptService cs = Context.getConceptService();
		
		// HIV stage 3 and 4
		List<Concept> hivStages = new ArrayList<Concept>();
		hivStages.add(cs.getConceptByUuid("1222AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		hivStages.add(cs.getConceptByUuid("1223AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		hivStages.add(cs.getConceptByUuid("1206AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		hivStages.add(cs.getConceptByUuid("1207AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		CodedObsCohortDefinition hivStage3And4Obs = new CodedObsCohortDefinition();
		hivStage3And4Obs.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		hivStage3And4Obs.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		hivStage3And4Obs.setOperator(SetComparator.IN);
		hivStage3And4Obs.setQuestion(cs.getConceptByUuid("5356AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
		hivStage3And4Obs.setValueList(hivStages);
		hivStage3And4Obs.setTimeModifier(TimeModifier.LAST);
		
		// Reffered out
		List<Concept> referallOption = new ArrayList<Concept>();
		referallOption.add(cs.getConceptByUuid("cf82933b-3f3f-45e7-a5ab-5d31aaee3da3")); //Yes
		CodedObsCohortDefinition referredObs = new CodedObsCohortDefinition();
		referredObs.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		referredObs.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		referredObs.setOperator(SetComparator.NOT_IN);
		referredObs.setQuestion(cs.getConceptByUuid("1648AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); //Referred
		referredObs.setValueList(referallOption);
		referredObs.setTimeModifier(TimeModifier.LAST);
		
		// Dead patients
		BirthAndDeathCohortDefinition livePatients = new BirthAndDeathCohortDefinition();
		livePatients.setDied(false);
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(visits, hivStage3And4Obs, referredObs, livePatients);
		hivStage3And4.addRow(MessageUtil.translate("commonreports.report.drc.hivStage.reportName"), ccd, parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		hivStage3And4.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		hivStage3And4.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		hivStage3And4.addColumn(col3, createCohortComposition(allGenders), null);
		Map<String, Object> ageParameterMappings = new HashMap<String, Object>();
		ageParameterMappings.put("effectiveDate", "${endDate}");
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col4, createCohortComposition(under1y), ageParameterMappings);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivStage3And4.addColumn(col11, createCohortComposition(_50andAbove), null);
		
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
		        .asList(ReportManagerUtil.createCsvReportDesign("beac741c-813c-43a0-80e1-27c9de5bcf01", reportDefinition));
	}
}
