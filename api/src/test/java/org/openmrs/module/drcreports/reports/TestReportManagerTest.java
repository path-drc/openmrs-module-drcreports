package org.openmrs.module.drcreports.reports;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.drcreports.ActivatedReportManager;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.initializer.api.loaders.Loader;
import org.openmrs.module.drcreports.reports.BaseModuleContextSensitiveMysqlBackedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TestReportManagerTest extends BaseModuleContextSensitiveMysqlBackedTest {
	
	public static final String COMPONENT_MOCK_REPORTMANAGER = "drcreports.mockReportManager";
	
	@Autowired
	@Qualifier(COMPONENT_MOCK_REPORTMANAGER)
	private ActivatedReportManager testReportManager;
	
	@Autowired
	private InitializerService iniz;
	
	@Before
	public void setUp() throws Exception {
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void isActive_shouldParseFromJsonConfig() {
		Assert.assertFalse(testReportManager.isActivated());
	}
}
