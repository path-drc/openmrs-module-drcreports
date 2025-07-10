package org.openmrs.module.drcreports.reports;

import static org.openmrs.module.drcreports.common.Helper.getStringFromResource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DisbursementReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.disbursement.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "72b05407-53d2-43e0-8ccc-ab6099dbe5ab";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.disbursement.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.disbursement.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		String startDate = new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.getStartOfMonth(new Date()));
		return new Parameter("startDate", "Start Date", Date.class, null, DateUtil.parseDate(startDate, "yyyy-MM-dd"));
	}
	
	private Parameter getEndDateParameter() {
		String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return new Parameter("endDate", "End Date", Date.class, null, DateUtil.parseDate(endDate, "yyyy-MM-dd"));
	}
	
	private Parameter getLocationParameter() {
		return new Parameter("locationList", "Visit Location", Location.class, List.class, null);
	}
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		params.add(getLocationParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		rd.setUuid(getUuid());
		
		SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
		sqlDsd.setName(MessageUtil.translate("drcreports.report.disbursement.datasetName"));
		sqlDsd.setDescription(MessageUtil.translate("drcreports.report.disbursement.datasetDescription"));
		
		String sql = getStringFromResource("org/openmrs/module/drcreports/sql/disbursement.sql");
		applyMetadataReplacements(sql);
		
		sqlDsd.setSqlQuery(sql);
		sqlDsd.addParameters(getParameters());
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("startDate", "${startDate}");
		parameterMappings.put("endDate", "${endDate}");
		parameterMappings.put("locationList", "${locationList}");
		
		rd.addDataSetDefinition(getName(), sqlDsd, parameterMappings);
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign reportDesign = ReportManagerUtil.createCsvReportDesign("77b6c2cb-4b96-47d8-bcde-b1b7f16f5670",
		    reportDefinition);
		return Arrays.asList(reportDesign);
	}
	
	private String applyMetadataReplacements(String rawSql) {
		Map<String, String> metadataReplacements = getMetadataReplacements();
		
		for (String key : metadataReplacements.keySet()) {
			rawSql = rawSql.replaceAll(":" + key, metadataReplacements.get(key));
		}
		return rawSql;
	}
	
	private Map<String, String> getMetadataReplacements() {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("ccsEncounterType", inizService.getValueFromKey("report.disbursement.ccs.encounter.type.uuid"));
		map.put("ncdEncounterTypeUuid", inizService.getValueFromKey("report.disbursement.ncd.encounter.type.uuid"));
		map.put("yesConceptUuid", inizService.getValueFromKey("report.disbursement.yes.concept.uuid"));
		map.put("positiveConceptUuid", inizService.getValueFromKey("report.disbursement.positive.concept.uuid"));
		map.put("viaDiagnosisQuestionConceptUuid",
		    inizService.getValueFromKey("report.disbursement.via.diagnosis.question.concept.uuid"));
		map.put("followupQuestionConceptUuid",
		    inizService.getValueFromKey("report.disbursement.followup.question.concept.uuid"));
		map.put("startedMedicationQuestionConceptUuid",
		    inizService.getValueFromKey("report.disbursement.started.medication.question.concept.uuid"));
		
		return map;
	}
	
}
