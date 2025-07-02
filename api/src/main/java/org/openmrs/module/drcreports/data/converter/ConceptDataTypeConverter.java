package org.openmrs.module.drcreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts a Concept id to Concept data type
 */
public class ConceptDataTypeConverter implements DataConverter {
	
	public ConceptDataTypeConverter() {
	}
	
	@Override
	public Object convert(Object original) {
		Obs o = Context.getObsService().getObs((Integer) original);
		
		if (o.hasGroupMembers()) {
			return "Group";
		}
		
		Concept c = o.getConcept();
		return c.getDatatype().getName();
	}
	
	@Override
	public Class<?> getInputDataType() {
		return Integer.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return Object.class;
	}
}
