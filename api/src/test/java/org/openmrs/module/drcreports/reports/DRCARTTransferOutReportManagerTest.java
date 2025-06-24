package org.openmrs.module.commonreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.ConceptService;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.initializer.api.loaders.Loader;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DRCARTTransferOutReportManagerTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private InitializerService iniz;
	
	@Autowired
	private ReportService rs;
	
	@Autowired
	private ReportDefinitionService rds;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Autowired
	private DRCARTTransferOutReportManager manager;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/DRCARTTransferOutTestDataset.xml");
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void setupReport_shouldSetupDRCARTTransferOutReport() {
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verify
		Assert.assertNotNull(rs.getReportDesignByUuid("3a6f873f-f5cc-4df7-b7af-9bc179bbb292"));
		
	}
	
	@Test
	public void testReport() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2025-06-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2025-06-30", "yyyy-MM-dd"));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			Map<String, Integer> columnValuePairs = getColumnValues();
			for (String column : columnValuePairs.keySet()) {
				assertThat(column, ((Cohort) row.getColumnValue(column)).getSize(), is(columnValuePairs.get(column)));
			}
		}
	}
	
	private Map<String, Integer> getColumnValues() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("DRC Patients Transferred on ART.Females", 1);
		map.put("DRC Patients Transferred on ART.Males", 0);
		map.put("DRC Patients Transferred on ART.All", 1);
		map.put("DRC Patients Transferred on ART.Below 1 year", 0);
		map.put("DRC Patients Transferred on ART.1-4 years", 0);
		map.put("DRC Patients Transferred on ART.5-9 years", 0);
		map.put("DRC Patients Transferred on ART.10-14 years", 0);
		map.put("DRC Patients Transferred on ART.15-19 years", 0);
		map.put("DRC Patients Transferred on ART.20-24 years", 0);
		map.put("DRC Patients Transferred on ART.25-49 years", 1);
		map.put("DRC Patients Transferred on ART.50+ years", 0);
		
		return map;
		
	}
}
