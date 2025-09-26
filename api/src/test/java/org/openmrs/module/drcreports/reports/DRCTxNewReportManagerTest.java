package org.openmrs.module.drcreports.reports;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DRCTxNewReportManagerTest extends BaseModuleContextSensitiveMysqlBackedTest {
	
	public DRCTxNewReportManagerTest() throws SQLException {
		super();
	}
	
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
	private DRCTxNewReportManager manager;
	
	@Override
	public void executeDataSet(IDataSet dataset) {
		try {
			Connection connection = getConnection();
			IDatabaseConnection dbUnitConn = setupDatabaseConnection(connection);
			DatabaseOperation.REFRESH.execute(dbUnitConn, dataset);
		}
		catch (Exception e) {
			throw new DatabaseUnitRuntimeException(e);
		}
	}
	
	private IDatabaseConnection setupDatabaseConnection(Connection connection) throws DatabaseUnitException {
		IDatabaseConnection dbUnitConn = new DatabaseConnection(connection);
		
		DatabaseConfig config = dbUnitConn.getConfig();
		config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
		
		return dbUnitConn;
	}
	
	@Before
	public void setUp() throws Exception {
		updateDatabase("org/openmrs/module/drcreports/liquibase/test-liquibase.xml");
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/drcreports/include/DRCTxNewReportTestDataset.xml");
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void setupReport_shouldCreateCsvDesign() throws Exception {
		// setup
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verify
		assertThat(rs.getReportDesignByUuid("20b98eb6-b42b-4a58-a240-9e81d8383814"), is(notNullValue()));
		
	}
	
	@Test
	public void testReport() throws Exception {
		// setup
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2024-01-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2024-12-31", "yyyy-MM-dd"));
		
		// replay
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		// verify
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
		
		map.put("DRC TX_NEW Report.Total", 7);
		
		// Below 1 year
		map.put("DRC TX_NEW Report.Below 1 year (Males)", 0);
		map.put("DRC TX_NEW Report.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("DRC TX_NEW Report.1-4 years (Males)", 0);
		map.put("DRC TX_NEW Report.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("DRC TX_NEW Report.5-9 years (Males)", 0);
		map.put("DRC TX_NEW Report.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("DRC TX_NEW Report.10-14 years (Males)", 0);
		map.put("DRC TX_NEW Report.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("DRC TX_NEW Report.15-19 years (Males)", 0);
		map.put("DRC TX_NEW Report.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("DRC TX_NEW Report.20-24 years (Males)", 0);
		map.put("DRC TX_NEW Report.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("DRC TX_NEW Report.25-29 years (Males)", 0);
		map.put("DRC TX_NEW Report.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("DRC TX_NEW Report.30-34 years (Males)", 2);
		map.put("DRC TX_NEW Report.30-34 years (Females)", 1);
		
		// 35–39 years
		map.put("DRC TX_NEW Report.35-39 years (Males)", 1);
		map.put("DRC TX_NEW Report.35-39 years (Females)", 2);
		
		// 40–44 years
		map.put("DRC TX_NEW Report.40-44 years (Males)", 0);
		map.put("DRC TX_NEW Report.40-44 years (Females)", 1);
		
		// 45–49 years
		map.put("DRC TX_NEW Report.45-49 years (Males)", 0);
		map.put("DRC TX_NEW Report.45-49 years (Females)", 0);
		
		// 50-54 years
		map.put("DRC TX_NEW Report.50-54 years (Males)", 0);
		map.put("DRC TX_NEW Report.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("DRC TX_NEW Report.55-59 years (Males)", 0);
		map.put("DRC TX_NEW Report.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("DRC TX_NEW Report.60-64 years (Males)", 0);
		map.put("DRC TX_NEW Report.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("DRC TX_NEW Report.65+ years (Males)", 0);
		map.put("DRC TX_NEW Report.65+ years (Females)", 0);
		
		map.put("CD4 < 200 cell/mm3.Total", 3);
		
		// Below 1 year
		map.put("CD4 < 200 cell/mm3.Below 1 year (Males)", 0);
		map.put("CD4 < 200 cell/mm3.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("CD4 < 200 cell/mm3.1-4 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("CD4 < 200 cell/mm3.5-9 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("CD4 < 200 cell/mm3.10-14 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("CD4 < 200 cell/mm3.15-19 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("CD4 < 200 cell/mm3.20-24 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("CD4 < 200 cell/mm3.25-29 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("CD4 < 200 cell/mm3.30-34 years (Males)", 1);
		map.put("CD4 < 200 cell/mm3.30-34 years (Females)", 0);
		
		// 35–39 years
		map.put("CD4 < 200 cell/mm3.35-39 years (Males)", 1);
		map.put("CD4 < 200 cell/mm3.35-39 years (Females)", 1);
		
		// 40–44 years
		map.put("CD4 < 200 cell/mm3.40-44 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.40-44 years (Females)", 0);
		
		// 45–49 years
		map.put("CD4 < 200 cell/mm3.45-49 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("CD4 < 200 cell/mm3.50-54 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("CD4 < 200 cell/mm3.55-59 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("CD4 < 200 cell/mm3.60-64 years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("CD4 < 200 cell/mm3.65+ years (Males)", 0);
		map.put("CD4 < 200 cell/mm3.65+ years (Females)", 0);
		
		map.put("CD4 >= 200 cell/mm3.Total", 2);
		
		// Below 1 year
		map.put("CD4 >= 200 cell/mm3.Below 1 year (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("CD4 >= 200 cell/mm3.1-4 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("CD4 >= 200 cell/mm3.5-9 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("CD4 >= 200 cell/mm3.10-14 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("CD4 >= 200 cell/mm3.15-19 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("CD4 >= 200 cell/mm3.20-24 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("CD4 >= 200 cell/mm3.25-29 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("CD4 >= 200 cell/mm3.30-34 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.30-34 years (Females)", 1);
		
		// 35–39 years
		map.put("CD4 >= 200 cell/mm3.35-39 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.35-39 years (Females)", 0);
		
		// 40–44 years
		map.put("CD4 >= 200 cell/mm3.40-44 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.40-44 years (Females)", 1);
		
		// 45–49 years
		map.put("CD4 >= 200 cell/mm3.45-49 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("CD4 >= 200 cell/mm3.50-54 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("CD4 >= 200 cell/mm3.55-59 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("CD4 >= 200 cell/mm3.60-64 years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("CD4 >= 200 cell/mm3.65+ years (Males)", 0);
		map.put("CD4 >= 200 cell/mm3.65+ years (Females)", 0);
		
		map.put("Unknown CD4 count.Total", 2);
		
		// Below 1 year
		map.put("Unknown CD4 count.Below 1 year (Males)", 0);
		map.put("Unknown CD4 count.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("Unknown CD4 count.1-4 years (Males)", 0);
		map.put("Unknown CD4 count.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("Unknown CD4 count.5-9 years (Males)", 0);
		map.put("Unknown CD4 count.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("Unknown CD4 count.10-14 years (Males)", 0);
		map.put("Unknown CD4 count.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("Unknown CD4 count.15-19 years (Males)", 0);
		map.put("Unknown CD4 count.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("Unknown CD4 count.20-24 years (Males)", 0);
		map.put("Unknown CD4 count.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("Unknown CD4 count.25-29 years (Males)", 0);
		map.put("Unknown CD4 count.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("Unknown CD4 count.30-34 years (Males)", 1);
		map.put("Unknown CD4 count.30-34 years (Females)", 0);
		
		// 35–39 years
		map.put("Unknown CD4 count.35-39 years (Males)", 0);
		map.put("Unknown CD4 count.35-39 years (Females)", 1);
		
		// 40–44 years
		map.put("Unknown CD4 count.40-44 years (Males)", 0);
		map.put("Unknown CD4 count.40-44 years (Females)", 0);
		
		// 45–49 years
		map.put("Unknown CD4 count.45-49 years (Males)", 0);
		map.put("Unknown CD4 count.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("Unknown CD4 count.50-54 years (Males)", 0);
		map.put("Unknown CD4 count.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("Unknown CD4 count.55-59 years (Males)", 0);
		map.put("Unknown CD4 count.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("Unknown CD4 count.60-64 years (Males)", 0);
		map.put("Unknown CD4 count.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("Unknown CD4 count.65+ years (Males)", 0);
		map.put("Unknown CD4 count.65+ years (Females)", 0);
		
		map.put("Breastfeeding at ART Initiation.Total", 2);
		
		// Below 1 year
		map.put("Breastfeeding at ART Initiation.Below 1 year (Males)", 0);
		map.put("Breastfeeding at ART Initiation.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("Breastfeeding at ART Initiation.1-4 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("Breastfeeding at ART Initiation.5-9 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("Breastfeeding at ART Initiation.10-14 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("Breastfeeding at ART Initiation.15-19 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("Breastfeeding at ART Initiation.20-24 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("Breastfeeding at ART Initiation.25-29 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("Breastfeeding at ART Initiation.30-34 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.30-34 years (Females)", 0);
		
		// 35–39 years
		map.put("Breastfeeding at ART Initiation.35-39 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.35-39 years (Females)", 2);
		
		// 40–44 years
		map.put("Breastfeeding at ART Initiation.40-44 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.40-44 years (Females)", 0);
		
		// 45–49 years
		map.put("Breastfeeding at ART Initiation.45-49 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("Breastfeeding at ART Initiation.50-54 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("Breastfeeding at ART Initiation.55-59 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("Breastfeeding at ART Initiation.60-64 years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("Breastfeeding at ART Initiation.65+ years (Males)", 0);
		map.put("Breastfeeding at ART Initiation.65+ years (Females)", 0);
		
		return map;
		
	}
	
	private void updateDatabase(String filename) throws Exception {
		Liquibase liquibase = getLiquibase(filename);
		liquibase.update("Modify column datatype to longblob on reporting_report_design_resource table");
		liquibase.getDatabase().getConnection().commit();
	}
	
	private Liquibase getLiquibase(String filename) throws Exception {
		Database liquibaseConnection = DatabaseFactory.getInstance()
		        .findCorrectDatabaseImplementation(new JdbcConnection(getConnection()));
		
		liquibaseConnection.setDatabaseChangeLogTableName("LIQUIBASECHANGELOG");
		liquibaseConnection.setDatabaseChangeLogLockTableName("LIQUIBASECHANGELOGLOCK");
		
		return new Liquibase(filename, new ClassLoaderResourceAccessor(getClass().getClassLoader()), liquibaseConnection);
	}
}
