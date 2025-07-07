package org.openmrs.module.drcreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

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
import org.springframework.stereotype.Component;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;

@Component
public class DRCArtTranferInReportManager extends ActivatedReportManager {
	
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
		return "45c4ffdb-c4ff-4024-9d72-b6ecb0c1cac1";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.drc.artTranferIn.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.drc.artTranferIn.reportDescription");
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
		
		// artTranferIn Grouping
		CohortCrossTabDataSetDefinition artTranferIn = new CohortCrossTabDataSetDefinition();
		artTranferIn.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(artTranferIn));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		SqlCohortDefinition sqd = new SqlCohortDefinition();
		
		// Patient Type at Enrolment -> Transfer-In
		// ART start date < enrollment date
		/////// enrollment date in reporting date range
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '83e40f2c-c316-43e6-a12e-20a338100281' AND c_answer.uuid = '160563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0) "
		        + "AND EXISTS (SELECT 1 FROM obs date_obs JOIN concept date_concept ON date_obs.concept_id = date_concept.concept_id WHERE date_obs.person_id = p.patient_id AND date_concept.uuid = '159599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND date_obs.voided = 0 AND date_obs.obs_datetime = (SELECT MAX(do_inner.obs_datetime) FROM obs do_inner WHERE do_inner.person_id = p.patient_id AND do_inner.concept_id = date_concept.concept_id AND do_inner.voided = 0) "
		        + "AND date_obs.value_datetime < (SELECT ref_date_obs.value_datetime FROM obs ref_date_obs JOIN concept ref_date_concept ON ref_date_obs.concept_id = ref_date_concept.concept_id WHERE ref_date_obs.person_id = p.patient_id AND ref_date_concept.uuid = '160555AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND ref_date_obs.voided = 0 AND ref_date_obs.value_datetime BETWEEN :onOrAfter AND :onOrBefore "
		        + "AND ref_date_obs.obs_datetime = (SELECT MAX(rdo_inner.obs_datetime) FROM obs rdo_inner WHERE rdo_inner.person_id = p.patient_id AND rdo_inner.concept_id = ref_date_concept.concept_id AND rdo_inner.voided = 0 AND rdo_inner.value_datetime BETWEEN :onOrAfter AND :onOrBefore)));";
		
		sqd.setQuery(sql);
		sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(sqd);
		artTranferIn.addRow(getName(), ccd, parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		artTranferIn.addColumn(col1, createCohortComposition(males), null);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		artTranferIn.addColumn(col2, createCohortComposition(females), null);
		
		GenderCohortDefinition allGenders = new GenderCohortDefinition();
		allGenders.setFemaleIncluded(true);
		allGenders.setMaleIncluded(true);
		artTranferIn.addColumn(col3, createCohortComposition(allGenders), null);
		
		// < 1 year
		AgeCohortDefinition under1y = new AgeCohortDefinition();
		under1y.setMinAge(0);
		under1y.setMinAgeUnit(DurationUnit.DAYS);
		under1y.setMaxAge(11);
		under1y.setMaxAgeUnit(DurationUnit.MONTHS);
		under1y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col4, createCohortComposition(under1y), null);
		
		// 1-4 years
		AgeCohortDefinition _1To4y = new AgeCohortDefinition();
		_1To4y.setMinAge(1);
		_1To4y.setMinAgeUnit(DurationUnit.YEARS);
		_1To4y.setMaxAge(4);
		_1To4y.setMaxAgeUnit(DurationUnit.YEARS);
		_1To4y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col5, createCohortComposition(_1To4y), null);
		
		// 5-9 years
		AgeCohortDefinition _5To9y = new AgeCohortDefinition();
		_5To9y.setMinAge(5);
		_5To9y.setMinAgeUnit(DurationUnit.YEARS);
		_5To9y.setMaxAge(9);
		_5To9y.setMaxAgeUnit(DurationUnit.YEARS);
		_5To9y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col6, createCohortComposition(_5To9y), null);
		
		// 10-14 years
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		_10To14y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col7, createCohortComposition(_10To14y), null);
		
		// 15-19 years
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		_15To19y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col8, createCohortComposition(_15To19y), null);
		
		// 20-24 years
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		_20To24y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col9, createCohortComposition(_20To24y), null);
		
		// 25-49 years
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_25To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col10, createCohortComposition(_25To49y), null);
		
		// 50+ years
		AgeCohortDefinition _50andAbove = new AgeCohortDefinition();
		_50andAbove.setMinAge(50);
		_50andAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50andAbove.setMaxAge(200);
		_50andAbove.setMaxAgeUnit(DurationUnit.YEARS);
		_50andAbove.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		artTranferIn.addColumn(col11, createCohortComposition(_50andAbove), null);
		
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
		        .asList(ReportManagerUtil.createCsvReportDesign("b95b1b84-91a8-4c1c-901f-fc84b1ad168f", reportDefinition));
	}
}
