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
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
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
public class DRCHIVCaScreeningReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		//return inizService.getBooleanFromKey("report.drc.hivCaScreening.active", false);
		return true;
		
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "ac8ce4df-3283-4ad1-a2f9-eac5c0c53f5e";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.hivCaScreening.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.drc.hivCaScreening.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", MessageUtil.translate("commonreports.report.util.reportingStartDate"), Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", MessageUtil.translate("commonreports.report.util.reportingEndDate"), Date.class);
	}
	
	public String getHivCaScreeningName() {
		return MessageUtil.translate("commonreports.report.drc.hivCaScreening.reportName");
	}
	
	public static String col1 = "";
	
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
		
		// hivCaScreening Grouping
		CohortCrossTabDataSetDefinition hivCaScreening = new CohortCrossTabDataSetDefinition();
		hivCaScreening.addParameters(getParameters());
		rd.addDataSetDefinition(getHivCaScreeningName(), Mapped.mapStraightThrough(hivCaScreening));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("startedOnOrAfter", "${startDate}");
		parameterMappings.put("startedOnOrBefore", "${endDate}");
		
		VisitCohortDefinition visits = new VisitCohortDefinition();
		visits.setVisitTypeList(vs.getAllVisitTypes(false));
		visits.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
		visits.addParameter(new Parameter("startedOnOrBefore", "On Or Before", Date.class));
		
		ConceptService cs = Context.getConceptService();
		
		// Cervical cancer screening performed
		CodedObsCohortDefinition caScreening = new CodedObsCohortDefinition();
		caScreening.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		caScreening.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		caScreening.setOperator(SetComparator.IN);
		caScreening.setQuestion(cs.getConceptByUuid("e5e99fc7-ff2d-4306-aefd-b87a07fc9ab4")); // Screened for cervical cancer during this visit
		caScreening.setTimeModifier(TimeModifier.LAST);
		List<Concept> caScreeningAnswers = new ArrayList<Concept>();
		caScreeningAnswers.add(cs.getConceptByUuid("165619AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")); // Cervical cancer screening performed
		caScreening.setValueList(caScreeningAnswers);
		
		SqlCohortDefinition ptLivingWithHIVsqd = new SqlCohortDefinition();
		
		//What do you want to do? ---> Enroll new Pt in HIV Care
		String sql = "SELECT DISTINCT p.patient_id FROM patient p WHERE p.voided = 0 "
		        + "AND EXISTS (SELECT 1 FROM obs o JOIN concept c_question ON o.concept_id = c_question.concept_id JOIN concept c_answer ON o.value_coded = c_answer.concept_id WHERE o.person_id = p.patient_id AND c_question.uuid = '83e40f2c-c316-43e6-a12e-20a338100281' AND c_answer.uuid = '164144AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' AND o.voided = 0);";
		ptLivingWithHIVsqd.setQuery(sql);
		
		// Alive patients
		BirthAndDeathCohortDefinition livePatients = new BirthAndDeathCohortDefinition();
		livePatients.setDied(false);
		
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(visits, ptLivingWithHIVsqd, caScreening, livePatients);
		
		hivCaScreening.addRow(MessageUtil.translate("commonreports.report.drc.caScreening"), ccd, parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		// 30-49 years
		AgeCohortDefinition _30To49y = new AgeCohortDefinition();
		_30To49y.setMinAge(30);
		_30To49y.setMinAgeUnit(DurationUnit.YEARS);
		_30To49y.setMaxAge(49);
		_30To49y.setMaxAgeUnit(DurationUnit.YEARS);
		_30To49y.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
		hivCaScreening.addColumn(col1, createCohortComposition(females, _30To49y), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("commonreports.report.drc.thirtyTofortyNineFemale.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("8fd7b2bb-94b4-4e69-8839-95cb4de1b4c2", reportDefinition));
	}
}
