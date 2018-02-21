package de.incentergy.architecture;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.core.access.model.JPAEdmMappingModelService;

public class StandardNavigationNamesJPAEdmMappingModelService extends JPAEdmMappingModelService {

	public StandardNavigationNamesJPAEdmMappingModelService(ODataJPAContext ctx) {
		super(ctx);
	}

	@Override
	public String mapJPARelationship(final String jpaEntityTypeName, final String jpaRelationshipName) {
		return jpaRelationshipName.substring(0, 1).toUpperCase() + jpaRelationshipName.substring(1);
	}

	@Override
	public boolean isMappingModelExists() {
		return true;
	}

	@Override
	public String mapJPAPersistenceUnit(final String persistenceUnitName) {
		return null;
	}
}
