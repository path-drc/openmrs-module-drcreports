package org.openmrs.module.drcreports.reports;

import static org.openmrs.module.drcreports.common.Helper.getStringFromResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition.TimeModifier;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.api.ConceptService;

import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BirthAndDeathCohortDefinition;

import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
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
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PresenceOrAbsenceCohortDefinition;
import org.openmrs.module.reporting.common.BooleanOperator;

@Component
public class DRCTx_CurrReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.drc.txCurr.active", true);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "d65e699d-e116-4e2c-8517-6d1ef30f7153";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.drc.txCurr.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.drc.txCurr.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("drcreports.report.util.reportingStartDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", MessageUtil.translate("drcreports.report.util.reportingEndDate"), Date.class);
	}
	
	public static String col0 = "";
	
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
	
	public static String col12 = "";
	
	public static String col13 = "";
	
	public static String col14 = "";
	
	public static String col15 = "";
	
	public static String col16 = "";
	
	public static String col17 = "";
	
	public static String col18 = "";
	
	public static String col19 = "";
	
	public static String col20 = "";
	
	public static String col21 = "";
	
	public static String col22 = "";
	
	public static String col23 = "";
	
	public static String col24 = "";
	
	public static String col25 = "";
	
	public static String col26 = "";
	
	public static String col27 = "";
	
	public static String col28 = "";
	
	public static String col29 = "";
	
	public static String col30 = "";
	
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
		
		// txCurr Grouping
		CohortCrossTabDataSetDefinition txCurr = new CohortCrossTabDataSetDefinition();
		txCurr.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(txCurr));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		//ConceptService cs = Context.getConceptService();
		
		// ART Initiated in date range
		SqlCohortDefinition artInitiationSqlCD = new SqlCohortDefinition();
		String artInitiationSql = getStringFromResource("org/openmrs/module/drcreports/sql/DRCTxCurrInitiation.sql");
		artInitiationSqlCD.setQuery(artInitiationSql);
		artInitiationSqlCD.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		artInitiationSqlCD.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Resume ART in date range
		SqlCohortDefinition resumeARTSqlCD = new SqlCohortDefinition();
		String resumeARTSql = getStringFromResource("org/openmrs/module/drcreports/sql/DRCTxCurrResumeART.sql");
		resumeARTSqlCD.setQuery(resumeARTSql);
		resumeARTSqlCD.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		resumeARTSqlCD.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Transfer In and PMTCT enrollment in date range
		SqlCohortDefinition transferInPMTCTSqlCD = new SqlCohortDefinition();
		String transferInPMTCTSql = getStringFromResource("org/openmrs/module/drcreports/sql/DRCTxCurrTransferPMTCT.sql");
		transferInPMTCTSqlCD.setQuery(transferInPMTCTSql);
		transferInPMTCTSqlCD.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		transferInPMTCTSqlCD.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		SqlCohortDefinition multiMonthSqlCD = new SqlCohortDefinition();
		String multiMonthSql = getStringFromResource("org/openmrs/module/drcreports/sql/DRCTxCurrMultiMonth.sql");
		multiMonthSqlCD.setQuery(multiMonthSql);
		multiMonthSqlCD.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		multiMonthSqlCD.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Not dead according to death attribute 
		BirthAndDeathCohortDefinition alive = new BirthAndDeathCohortDefinition();
		alive.setDied(false);
		//Not dead according to death form
		SqlCohortDefinition liveSqlCD = new SqlCohortDefinition();
		String liveSql = getStringFromResource("org/openmrs/module/drcreports/sql/DRCLivePatients.sql");
		liveSqlCD.setQuery(liveSql);
		
		// Not stopped ART in date range
		SqlCohortDefinition notStoppedARTSqlCD = new SqlCohortDefinition();
		String notStoppedARTSql = getStringFromResource("org/openmrs/module/drcreports/sql/DRCTxCurrNotStopedART.sql");
		notStoppedARTSqlCD.setQuery(notStoppedARTSql);
		notStoppedARTSqlCD.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		notStoppedARTSqlCD.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		// Not Transferred out in date range
		SqlCohortDefinition notTransferredOutSqlCD = new SqlCohortDefinition();
		String notTransferredOutSql = getStringFromResource(
		    "org/openmrs/module/drcreports/sql/DRCTxCurrNotTransferredOut.sql");
		notTransferredOutSqlCD.setQuery(notTransferredOutSql);
		notTransferredOutSqlCD.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		notTransferredOutSqlCD.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		// Composition for the OR condition (artInitiation OR resumeARTSqlCD OR transferInPMTCT OR multiMonth)
		CompositionCohortDefinition atleastInArtInitiationOrTransferInOrPMTCTOrMultiMonthCD = new CompositionCohortDefinition();
		atleastInArtInitiationOrTransferInOrPMTCTOrMultiMonthCD.initializeFromQueries(BooleanOperator.OR, artInitiationSqlCD,
		    resumeARTSqlCD, transferInPMTCTSqlCD, multiMonthSqlCD);
		// OR condition with the AND conditions (alive AND live)
		ccd.initializeFromElements(atleastInArtInitiationOrTransferInOrPMTCTOrMultiMonthCD, alive, liveSqlCD,
		    notStoppedARTSqlCD, notTransferredOutSqlCD);
		
		CompositionCohortDefinition ccd1 = new CompositionCohortDefinition();
		ccd1.initializeFromElements(artInitiationSqlCD, alive, liveSqlCD);
		
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(transferInPMTCTSqlCD, alive, liveSqlCD);
		
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(multiMonthSqlCD, alive, liveSqlCD);
		
		CompositionCohortDefinition ccd4 = new CompositionCohortDefinition();
		ccd4.initializeFromElements(liveSqlCD, alive, liveSqlCD);
		
		CompositionCohortDefinition ccd5 = new CompositionCohortDefinition();
		ccd5.initializeFromElements(resumeARTSqlCD, alive, liveSqlCD);
		
		txCurr.addRow(getName(), ccd, parameterMappings);
		txCurr.addRow("Art Init", ccd1, parameterMappings);
		txCurr.addRow("Transfer In", ccd2, parameterMappings);
		txCurr.addRow("Multimonth", ccd3, parameterMappings);
		txCurr.addRow("Resume ART", ccd5, parameterMappings);
		
		txCurr.addRow("live", ccd4, null);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		
		txCurr.addColumn(col0, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col1, createCohortComposition(under1y, males), null);
		txCurr.addColumn(col2, createCohortComposition(under1y, females), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col3, createCohortComposition(_1To4y, males), null);
		txCurr.addColumn(col4, createCohortComposition(_1To4y, females), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col5, createCohortComposition(_5To9y, males), null);
		txCurr.addColumn(col6, createCohortComposition(_5To9y, females), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col7, createCohortComposition(_10To14y, males), null);
		txCurr.addColumn(col8, createCohortComposition(_10To14y, females), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col9, createCohortComposition(_15To19y, males), null);
		txCurr.addColumn(col10, createCohortComposition(_15To19y, females), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col11, createCohortComposition(_20To24y, males), null);
		txCurr.addColumn(col12, createCohortComposition(_20To24y, females), null);
		
		// 25-49 years
		AgeCohortDefinition _25To29y = new AgeCohortDefinition();
		_25To29y.setMinAge(25);
		_25To29y.setMinAgeUnit(DurationUnit.YEARS);
		_25To29y.setMaxAge(29);
		_25To29y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To29y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col13, createCohortComposition(_25To29y, males), null);
		txCurr.addColumn(col14, createCohortComposition(_25To29y, females), null);
		
		// 30-34 years
		AgeCohortDefinition _30To34y = new AgeCohortDefinition();
		_30To34y.setMinAge(30);
		_30To34y.setMinAgeUnit(DurationUnit.YEARS);
		_30To34y.setMaxAge(34);
		_30To34y.setMaxAgeUnit(DurationUnit.YEARS);
		_30To34y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col15, createCohortComposition(_30To34y, males), null);
		txCurr.addColumn(col16, createCohortComposition(_30To34y, females), null);
		
		// 35-39 years
		AgeCohortDefinition _35To39y = new AgeCohortDefinition();
		_35To39y.setMinAge(35);
		_35To39y.setMinAgeUnit(DurationUnit.YEARS);
		_35To39y.setMaxAge(39);
		_35To39y.setMaxAgeUnit(DurationUnit.YEARS);
		_35To39y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col17, createCohortComposition(_35To39y, males), null);
		txCurr.addColumn(col18, createCohortComposition(_35To39y, females), null);
		
		// 40-44 years
		AgeCohortDefinition _40To44y = new AgeCohortDefinition();
		_40To44y.setMinAge(40);
		_40To44y.setMinAgeUnit(DurationUnit.YEARS);
		_40To44y.setMaxAge(44);
		_40To44y.setMaxAgeUnit(DurationUnit.YEARS);
		_40To44y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col19, createCohortComposition(_40To44y, males), null);
		txCurr.addColumn(col20, createCohortComposition(_40To44y, females), null);
		
		// 45-49 years
		AgeCohortDefinition _45To49y = new AgeCohortDefinition();
		_45To49y.setMinAge(45);
		_45To49y.setMinAgeUnit(DurationUnit.YEARS);
		_45To49y.setMaxAge(49);
		_45To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_45To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col21, createCohortComposition(_45To49y, males), null);
		txCurr.addColumn(col22, createCohortComposition(_45To49y, females), null);
		
		// 50-54 years
		AgeCohortDefinition _50To54y = new AgeCohortDefinition();
		_50To54y.setMinAge(50);
		_50To54y.setMinAgeUnit(DurationUnit.YEARS);
		_50To54y.setMaxAge(54);
		_50To54y.setMaxAgeUnit(DurationUnit.YEARS);
		_50To54y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col23, createCohortComposition(_50To54y, males), null);
		txCurr.addColumn(col24, createCohortComposition(_50To54y, females), null);
		
		// 55-59 years
		AgeCohortDefinition _55To59y = new AgeCohortDefinition();
		_55To59y.setMinAge(55);
		_55To59y.setMinAgeUnit(DurationUnit.YEARS);
		_55To59y.setMaxAge(59);
		_55To59y.setMaxAgeUnit(DurationUnit.YEARS);
		_55To59y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col25, createCohortComposition(_55To59y, males), null);
		txCurr.addColumn(col26, createCohortComposition(_55To59y, females), null);
		
		// 60-64 years
		AgeCohortDefinition _60To64y = new AgeCohortDefinition();
		_60To64y.setMinAge(60);
		_60To64y.setMinAgeUnit(DurationUnit.YEARS);
		_60To64y.setMaxAge(64);
		_60To64y.setMaxAgeUnit(DurationUnit.YEARS);
		_60To64y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col27, createCohortComposition(_60To64y, males), null);
		txCurr.addColumn(col28, createCohortComposition(_60To64y, females), null);
		
		// 65+ years
		AgeCohortDefinition _66To69y = new AgeCohortDefinition();
		_66To69y.setMinAge(65);
		_66To69y.setMinAgeUnit(DurationUnit.YEARS);
		_66To69y.setMaxAge(200);
		_66To69y.setMaxAgeUnit(DurationUnit.YEARS);
		_66To69y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		txCurr.addColumn(col29, createCohortComposition(_66To69y, males), null);
		txCurr.addColumn(col30, createCohortComposition(_66To69y, females), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		col0 = MessageUtil.translate("drcreports.report.drc.total.label");
		
		col1 = MessageUtil.translate("drcreports.report.drc.belowOneYrMales.label");
		col2 = MessageUtil.translate("drcreports.report.drc.belowOneYrFemales.label");
		
		col3 = MessageUtil.translate("drcreports.report.drc.oneToFourYrsMales.label");
		col4 = MessageUtil.translate("drcreports.report.drc.oneToFourYrsFemales.label");
		col5 = MessageUtil.translate("drcreports.report.drc.fiveToNineYrsMales.label");
		col6 = MessageUtil.translate("drcreports.report.drc.fiveToNineYrsFemales.label");
		col7 = MessageUtil.translate("drcreports.report.drc.tenToFourteenYrsMales.label");
		col8 = MessageUtil.translate("drcreports.report.drc.tenToFourteenYrsFemales.label");
		col9 = MessageUtil.translate("drcreports.report.drc.fifteenToNineteenYrsMales.label");
		col10 = MessageUtil.translate("drcreports.report.drc.fifteenToNineteenYrsFemales.label");
		
		col11 = MessageUtil.translate("drcreports.report.drc.twentyToTwentyFourYrsMales.label");
		col12 = MessageUtil.translate("drcreports.report.drc.twentyToTwentyFourYrsFemales.label");
		col13 = MessageUtil.translate("drcreports.report.drc.twentyFiveToTwentyNineYrsMales.label");
		col14 = MessageUtil.translate("drcreports.report.drc.twentyFiveToTwentyNineYrsFemales.label");
		
		col15 = MessageUtil.translate("drcreports.report.drc.thirtyToThirtyFourYrsMales.label");
		col16 = MessageUtil.translate("drcreports.report.drc.thirtyToThirtyFourYrsFemales.label");
		col17 = MessageUtil.translate("drcreports.report.drc.thirtyFiveToThirtyNineYrsMales.label");
		col18 = MessageUtil.translate("drcreports.report.drc.thirtyFiveToThirtyNineYrsFemales.label");
		col19 = MessageUtil.translate("drcreports.report.drc.fortyToFortyFourYrsMales.label");
		col20 = MessageUtil.translate("drcreports.report.drc.fortyToFortyFourYrsFemales.label");
		col21 = MessageUtil.translate("drcreports.report.drc.fortyFiveToFortyNineYrsMales.label");
		col22 = MessageUtil.translate("drcreports.report.drc.fortyFiveToFortyNineYrsFemales.label");
		
		col23 = MessageUtil.translate("drcreports.report.drc.fiftyToFiftyFourYrsMales.label");
		col24 = MessageUtil.translate("drcreports.report.drc.fiftyToFiftyFourYrsFemales.label");
		col25 = MessageUtil.translate("drcreports.report.drc.fiftyFiveToFiftyNineYrsMales.label");
		col26 = MessageUtil.translate("drcreports.report.drc.fiftyFiveToFiftyNineYrsFemales.label");
		col27 = MessageUtil.translate("drcreports.report.drc.sixtyToSixtyFourYrsMales.label");
		col28 = MessageUtil.translate("drcreports.report.drc.sixtyToSixtyFourYrsFemales.label");
		col29 = MessageUtil.translate("drcreports.report.drc.sixtyFiveAndAboveMales.label");
		col30 = MessageUtil.translate("drcreports.report.drc.sixtyFiveAndAboveFemales.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("cb45687b-51b1-4d5d-a6a1-7a29ffd4d314", reportDefinition));
		
	}
}
