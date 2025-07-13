package org.openmrs.module.drcreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;

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

@Component
public class DRCCd4MonitoringReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.drc.cd4Monitoring.active", true);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "45631da8-1e16-414f-9894-4aafad370c77";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.drc.cd4Monitoring.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.drc.cd4Monitoring.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("drcreports.report.util.reportingStartDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", MessageUtil.translate("drcreports.report.util.reportingEndDate"), Date.class);
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
		
		// cd4Monitoring Grouping
		CohortCrossTabDataSetDefinition cd4Monitoring = new CohortCrossTabDataSetDefinition();
		cd4Monitoring.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(cd4Monitoring));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		SqlCohortDefinition sqd = new SqlCohortDefinition();
		
		// CD4 Result Date in range
		// CD4 count exists for the obs_date of CD4 Result Date above
		// Enrolled for HIV care program
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o_date JOIN concept c_date ON o_date.concept_id = c_date.concept_id WHERE o_date.person_id = p.patient_id AND c_date.uuid = '163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o_date.voided = 0 AND o_date.value_datetime IS NOT NULL AND o_date.value_datetime BETWEEN :onOrAfter AND :onOrBefore) "
		        + "AND EXISTS (SELECT 1 FROM obs o_num JOIN concept c_num ON o_num.concept_id = c_num.concept_id JOIN obs o_date ON o_date.person_id = o_num.person_id AND DATE(o_num.obs_datetime) = DATE(o_date.obs_datetime) JOIN concept c_date ON o_date.concept_id = c_date.concept_id WHERE o_num.person_id = p.patient_id AND c_num.uuid = '5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_date.uuid = '163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o_num.voided = 0 AND o_date.voided = 0 AND o_num.value_numeric IS NOT NULL AND o_date.value_datetime BETWEEN :onOrAfter AND :onOrBefore) "
		        + "AND EXISTS (SELECT 1 FROM patient_program pp JOIN program prog ON pp.program_id = prog.program_id WHERE pp.patient_id = p.patient_id AND prog.uuid = '64f950e6-1b07-4ac0-8e7e-f3e148f3463f' AND pp.voided = 0 AND pp.date_completed IS NULL);";
		
		sqd.setQuery(sql);
		sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(sqd);
		
		SqlCohortDefinition sqd2 = new SqlCohortDefinition();
		
		// CD4 Result Date in range
		// CD4 count exists for the obs_date of CD4 Result Date above as <200
		// Enrolled for HIV care program
		String sql2 = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o_date JOIN concept c_date ON o_date.concept_id = c_date.concept_id WHERE o_date.person_id = p.patient_id AND c_date.uuid = '163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o_date.voided = 0 AND o_date.value_datetime IS NOT NULL AND o_date.value_datetime BETWEEN :onOrAfter AND :onOrBefore) "
		        + "AND EXISTS (SELECT 1 FROM obs o_num JOIN concept c_num ON o_num.concept_id = c_num.concept_id JOIN obs o_date ON o_date.person_id = o_num.person_id AND DATE(o_num.obs_datetime) = DATE(o_date.obs_datetime) JOIN concept c_date ON o_date.concept_id = c_date.concept_id WHERE o_num.person_id = p.patient_id AND c_num.uuid = '5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND c_date.uuid = '163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o_num.voided = 0 AND o_date.voided = 0 AND o_num.value_numeric IS NOT NULL AND o_num.value_numeric < 200 AND o_date.value_datetime BETWEEN :onOrAfter AND :onOrBefore) "
		        + "AND EXISTS (SELECT 1 FROM patient_program pp JOIN program prog ON pp.program_id = prog.program_id WHERE pp.patient_id = p.patient_id AND prog.uuid = '64f950e6-1b07-4ac0-8e7e-f3e148f3463f' AND pp.voided = 0 AND pp.date_completed IS NULL);";
		sqd2.setQuery(sql2);
		sqd2.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd2.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(sqd2);
		
		cd4Monitoring.addRow(getName(), ccd, parameterMappings);
		cd4Monitoring.addRow(MessageUtil.translate("drcreports.report.drc.cd4MonitoringBelow200"), ccd2, parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		cd4Monitoring.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		cd4Monitoring.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		cd4Monitoring.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		cd4Monitoring.addColumn(col11, createCohortComposition(_50andAbove), null);
		
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
		        .asList(ReportManagerUtil.createCsvReportDesign("33868713-461d-47d5-b1f5-40bdb2e878e3", reportDefinition));
	}
}
