package org.openmrs.module.drcreports.reports;

import static org.openmrs.module.drcreports.common.Helper.getStringFromResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MSPPVisitsReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("visitService")
	private VisitService vs;
	
	@Autowired
	@Qualifier("initializer.InitializerService")
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.MSPP.visits.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "8613f380-8f39-492f-b46b-9802482bc315";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("drcreports.report.MSPP.visits.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("drcreports.report.MSPP.visits.reportDescription");
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
		
		ReportDefinition rd = new ReportDefinition();
		
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		rd.setUuid(getUuid());
		
		SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
		sqlDsd.setName(getName());
		sqlDsd.setDescription("");
		
		String sql = getStringFromResource("org/openmrs/module/drcreports/sql/MSPPvisits.sql");
		sql = applyMetadataReplacements(sql);
		
		sqlDsd.setSqlQuery(sql);
		sqlDsd.addParameters(getParameters());
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("startDate", "${startDate}");
		parameterMappings.put("endDate", "${endDate}");
		
		rd.addDataSetDefinition(getName(), sqlDsd, parameterMappings);
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign reportDesign = ReportManagerUtil.createExcelTemplateDesign("4e1c1f7a-bbfe-44ec-a36a-3005e3fc50bd",
		    reportDefinition, "org/openmrs/module/drcreports/reportTemplates/visitsReportTemplate.xls");
		Properties designProperties = new Properties();
		designProperties.put("repeatingSections", "sheet:1,row:7,dataset:" + getName());
		designProperties.put("columnTranslationLocale", Context.getLocale().toString());
		designProperties.put("reportName.label", MessageUtil.translate("drcreports.report.MSPP.visits.reportName"));
		designProperties.put("date.range.label", MessageUtil.translate("drcreports.report.MSPP.visits.date.range.label"));
		designProperties.put("to.label", MessageUtil.translate("drcreports.report.MSPP.visits.to.label"));
		designProperties.put("natureOfVisits.label",
		    MessageUtil.translate("drcreports.report.MSPP.visits.natureOfVisits.label"));
		designProperties.put("newVisits.label", MessageUtil.translate("drcreports.report.MSPP.visits.newVisits.label"));
		designProperties.put("subsequentVisits.label",
		    MessageUtil.translate("drcreports.report.MSPP.visits.subsequentVisits.label"));
		designProperties.put("categories.label", MessageUtil.translate("drcreports.report.MSPP.visits.categories.label"));
		reportDesign.setProperties(designProperties);
		return Arrays.asList(reportDesign);
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
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
		String prenatalVisitTypeUuid = inizService.getValueFromKey("report.MSPP.visits.prenatal.visitType.uuid");
		String familyPlanningVisitTypeUuid = inizService.getValueFromKey("report.MSPP.visits.familyPlanning.visitType.uuid");
		String[] properties = { "drcreports.report.MSPP.visits.category1.label",
		        "drcreports.report.MSPP.visits.category2.label", "drcreports.report.MSPP.visits.category3.label",
		        "drcreports.report.MSPP.visits.category4.label", "drcreports.report.MSPP.visits.category5.label",
		        "drcreports.report.MSPP.visits.category6.label", "drcreports.report.MSPP.visits.category7.label",
		        "drcreports.report.MSPP.visits.category8.label", "drcreports.report.MSPP.visits.category9.label",
		        "drcreports.report.MSPP.visits.total.label" };
		
		for (String prop : properties) {
			map.put(prop, MessageUtil.translate(prop));
		}
		map.put("familyPlanningVisitTypeId", vs.getVisitTypeByUuid(familyPlanningVisitTypeUuid).getId().toString());
		map.put("prenatalVisitTypeId", vs.getVisitTypeByUuid(prenatalVisitTypeUuid).getId().toString());
		
		return map;
	}
}
