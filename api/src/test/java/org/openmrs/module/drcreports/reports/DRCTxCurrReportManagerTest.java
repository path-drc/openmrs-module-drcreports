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

public class DRCTxCurrReportManagerTest extends BaseModuleContextSensitiveMysqlBackedTest {
	
	public DRCTxCurrReportManagerTest() throws SQLException {
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
	private DRCTxCurrReportManager manager;
	
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
		executeDataSet("org/openmrs/module/drcreports/include/DRCTxCurrTestDataset.xml");
		
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
		assertThat(rs.getReportDesignByUuid("cb45687b-51b1-4d5d-a6a1-7a29ffd4d314"), is(notNullValue()));
		
	}
	
	@Test
	public void testReport() throws Exception {
		// setup
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2025-06-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2025-08-31", "yyyy-MM-dd"));
		
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
		
		map.put("DRC TX_CURR Report.Total", 17);
		
		// Below 1 year
		map.put("DRC TX_CURR Report.Below 1 year (Males)", 0);
		map.put("DRC TX_CURR Report.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("DRC TX_CURR Report.1-4 years (Males)", 0);
		map.put("DRC TX_CURR Report.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("DRC TX_CURR Report.5-9 years (Males)", 0);
		map.put("DRC TX_CURR Report.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("DRC TX_CURR Report.10-14 years (Males)", 0);
		map.put("DRC TX_CURR Report.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("DRC TX_CURR Report.15-19 years (Males)", 0);
		map.put("DRC TX_CURR Report.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("DRC TX_CURR Report.20-24 years (Males)", 0);
		map.put("DRC TX_CURR Report.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("DRC TX_CURR Report.25-29 years (Males)", 0);
		map.put("DRC TX_CURR Report.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("DRC TX_CURR Report.30-34 years (Males)", 1);
		map.put("DRC TX_CURR Report.30-34 years (Females)", 3);
		
		// 35–39 years
		map.put("DRC TX_CURR Report.35-39 years (Males)", 6);
		map.put("DRC TX_CURR Report.35-39 years (Females)", 5);
		
		// 40–44 years
		map.put("DRC TX_CURR Report.40-44 years (Males)", 1);
		map.put("DRC TX_CURR Report.40-44 years (Females)", 1);
		
		// 45–49 years
		map.put("DRC TX_CURR Report.45-49 years (Males)", 0);
		map.put("DRC TX_CURR Report.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("DRC TX_CURR Report.50-54 years (Males)", 0);
		map.put("DRC TX_CURR Report.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("DRC TX_CURR Report.55-59 years (Males)", 0);
		map.put("DRC TX_CURR Report.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("DRC TX_CURR Report.60-64 years (Males)", 0);
		map.put("DRC TX_CURR Report.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("DRC TX_CURR Report.65+ years (Males)", 0);
		map.put("DRC TX_CURR Report.65+ years (Females)", 0);
		
		// Below 15 years
		map.put("DRC TX_CURR Report.Below 15 years (Males)", 0);
		map.put("DRC TX_CURR Report.Below 15 years (Females)", 0);
		
		// 15+ years
		map.put("DRC TX_CURR Report.15+ years (Males)", 8);
		map.put("DRC TX_CURR Report.15+ years (Females)", 9);
		
		map.put("< 3 months drugs.Total", 4);
		
		// Below 1 year
		map.put("< 3 months drugs.Below 1 year (Males)", 0);
		map.put("< 3 months drugs.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("< 3 months drugs.1-4 years (Males)", 0);
		map.put("< 3 months drugs.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("< 3 months drugs.5-9 years (Males)", 0);
		map.put("< 3 months drugs.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("< 3 months drugs.10-14 years (Males)", 0);
		map.put("< 3 months drugs.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("< 3 months drugs.15-19 years (Males)", 0);
		map.put("< 3 months drugs.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("< 3 months drugs.20-24 years (Males)", 0);
		map.put("< 3 months drugs.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("< 3 months drugs.25-29 years (Males)", 0);
		map.put("< 3 months drugs.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("< 3 months drugs.30-34 years (Males)", 0);
		map.put("< 3 months drugs.30-34 years (Females)", 1);
		
		// 35–39 years
		map.put("< 3 months drugs.35-39 years (Males)", 2);
		map.put("< 3 months drugs.35-39 years (Females)", 1);
		
		// 40–44 years
		map.put("< 3 months drugs.40-44 years (Males)", 0);
		map.put("< 3 months drugs.40-44 years (Females)", 0);
		
		// 45–49 years
		map.put("< 3 months drugs.45-49 years (Males)", 0);
		map.put("< 3 months drugs.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("< 3 months drugs.50-54 years (Males)", 0);
		map.put("< 3 months drugs.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("< 3 months drugs.55-59 years (Males)", 0);
		map.put("< 3 months drugs.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("< 3 months drugs.60-64 years (Males)", 0);
		map.put("< 3 months drugs.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("< 3 months drugs.65+ years (Males)", 0);
		map.put("< 3 months drugs.65+ years (Females)", 0);
		
		// Below 15 years
		map.put("< 3 months drugs.Below 15 years (Males)", 0);
		map.put("< 3 months drugs.Below 15 years (Females)", 0);
		
		// 15+ years
		map.put("< 3 months drugs.15+ years (Males)", 2);
		map.put("< 3 months drugs.15+ years (Females)", 2);
		
		map.put("3-5 months drugs.Total", 3);
		
		// Below 1 year
		map.put("3-5 months drugs.Below 1 year (Males)", 0);
		map.put("3-5 months drugs.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("3-5 months drugs.1-4 years (Males)", 0);
		map.put("3-5 months drugs.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("3-5 months drugs.5-9 years (Males)", 0);
		map.put("3-5 months drugs.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("3-5 months drugs.10-14 years (Males)", 0);
		map.put("3-5 months drugs.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("3-5 months drugs.15-19 years (Males)", 0);
		map.put("3-5 months drugs.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("3-5 months drugs.20-24 years (Males)", 0);
		map.put("3-5 months drugs.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("3-5 months drugs.25-29 years (Males)", 0);
		map.put("3-5 months drugs.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("3-5 months drugs.30-34 years (Males)", 1);
		map.put("3-5 months drugs.30-34 years (Females)", 0);
		
		// 35–39 years
		map.put("3-5 months drugs.35-39 years (Males)", 0);
		map.put("3-5 months drugs.35-39 years (Females)", 1);
		
		// 40–44 years
		map.put("3-5 months drugs.40-44 years (Males)", 0);
		map.put("3-5 months drugs.40-44 years (Females)", 1);
		
		// 45–49 years
		map.put("3-5 months drugs.45-49 years (Males)", 0);
		map.put("3-5 months drugs.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("3-5 months drugs.50-54 years (Males)", 0);
		map.put("3-5 months drugs.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("3-5 months drugs.55-59 years (Males)", 0);
		map.put("3-5 months drugs.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("3-5 months drugs.60-64 years (Males)", 0);
		map.put("3-5 months drugs.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("3-5 months drugs.65+ years (Males)", 0);
		map.put("3-5 months drugs.65+ years (Females)", 0);
		
		// Below 15 years
		map.put("3-5 months drugs.Below 15 years (Males)", 0);
		map.put("3-5 months drugs.Below 15 years (Females)", 0);
		
		// 15+ years
		map.put("3-5 months drugs.15+ years (Males)", 1);
		map.put("3-5 months drugs.15+ years (Females)", 2);
		
		map.put("6+ months drugs.Total", 0);
		
		// Below 1 year
		map.put("6+ months drugs.Below 1 year (Males)", 0);
		map.put("6+ months drugs.Below 1 year (Females)", 0);
		
		// 1–4 years
		map.put("6+ months drugs.1-4 years (Males)", 0);
		map.put("6+ months drugs.1-4 years (Females)", 0);
		
		// 5–9 years
		map.put("6+ months drugs.5-9 years (Males)", 0);
		map.put("6+ months drugs.5-9 years (Females)", 0);
		
		// 10–14 years
		map.put("6+ months drugs.10-14 years (Males)", 0);
		map.put("6+ months drugs.10-14 years (Females)", 0);
		
		// 15–19 years
		map.put("6+ months drugs.15-19 years (Males)", 0);
		map.put("6+ months drugs.15-19 years (Females)", 0);
		
		// 20–24 years
		map.put("6+ months drugs.20-24 years (Males)", 0);
		map.put("6+ months drugs.20-24 years (Females)", 0);
		
		// 25–29 years
		map.put("6+ months drugs.25-29 years (Males)", 0);
		map.put("6+ months drugs.25-29 years (Females)", 0);
		
		// 30–34 years
		map.put("6+ months drugs.30-34 years (Males)", 0);
		map.put("6+ months drugs.30-34 years (Females)", 0);
		
		// 35–39 years
		map.put("6+ months drugs.35-39 years (Males)", 0);
		map.put("6+ months drugs.35-39 years (Females)", 0);
		
		// 40–44 years
		map.put("6+ months drugs.40-44 years (Males)", 0);
		map.put("6+ months drugs.40-44 years (Females)", 0);
		
		// 45–49 years
		map.put("6+ months drugs.45-49 years (Males)", 0);
		map.put("6+ months drugs.45-49 years (Females)", 0);
		
		// 50–54 years
		map.put("6+ months drugs.50-54 years (Males)", 0);
		map.put("6+ months drugs.50-54 years (Females)", 0);
		
		// 55–59 years
		map.put("6+ months drugs.55-59 years (Males)", 0);
		map.put("6+ months drugs.55-59 years (Females)", 0);
		
		// 60–64 years
		map.put("6+ months drugs.60-64 years (Males)", 0);
		map.put("6+ months drugs.60-64 years (Females)", 0);
		
		// 65+ years
		map.put("6+ months drugs.65+ years (Males)", 0);
		map.put("6+ months drugs.65+ years (Females)", 0);
		
		// Below 15 years
		map.put("6+ months drugs.Below 15 years (Males)", 0);
		map.put("6+ months drugs.Below 15 years (Females)", 0);
		
		// 15+ years
		map.put("6+ months drugs.15+ years (Males)", 0);
		map.put("6+ months drugs.15+ years (Females)", 0);
		
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
