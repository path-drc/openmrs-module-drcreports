package org.openmrs.module.drcreports.common;

import org.openmrs.annotation.AddOnStartup;
import org.openmrs.annotation.HasAddOnStartupPrivileges;

@HasAddOnStartupPrivileges
public class DRCReportsPrivilegeConstants {
	
	@AddOnStartup(description = "Able to view patient history report")
	public static final String VIEW_PATIENT_HISTORY = "App: Can View Patient History Report";
	
}
