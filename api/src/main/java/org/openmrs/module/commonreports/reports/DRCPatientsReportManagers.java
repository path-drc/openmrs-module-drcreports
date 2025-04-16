package org.openmrs.module.commonreports.reports;

import static org.openmrs.module.commonreports.common.Helper.getStringFromResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;

@Component
public class DRCPatientsReportManagers extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		//return inizService.getBooleanFromKey("report.patients.active", true);
		return true;
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "6b9898bc-0fbb-4b46-92b7-fdc45e655b7b";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.drc.patients.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.patients.reportDescription");
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
		
		ReportDefinition rd = new ReportDefinition();
		
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		rd.setUuid(getUuid());
		
		SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
		sqlDsd.setName(MessageUtil.translate("commonreports.report.drc.patients.datasetName"));
		sqlDsd.setDescription(MessageUtil.translate("commonreports.report.patients.datasetDescription"));
		
		String sql = getStringFromResource("org/openmrs/module/commonreports/sql/DRCpatients.sql");
		
		sqlDsd.setSqlQuery(sql);
		sqlDsd.addParameters(getParameters());
		
		rd.addDataSetDefinition("DRCReport", Mapped.mapStraightThrough(sqlDsd));
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		List<ReportDesign> l = new ArrayList<ReportDesign>();
		ReportDesign design = ReportManagerUtil.createExcelTemplateDesign("1462401f-c21f-4d12-b4f6-936d9cf76d5b",
		    reportDefinition, "org/openmrs/module/commonreports/reportTemplates/drcAdvanceHIV.xls");
		design.addPropertyValue("repeatingSections", "sheet:1,row:5,dataset: DRCReport");
		l.add(design);
		return l;
	}
	
}
