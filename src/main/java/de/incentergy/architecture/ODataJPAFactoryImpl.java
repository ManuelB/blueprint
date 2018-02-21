package de.incentergy.architecture;

import java.util.Locale;

import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPADefaultProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAResponseBuilder;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmMappingModelAccess;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAProcessor;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAMessageService;
import org.apache.olingo.odata2.jpa.processor.api.factory.JPAAccessFactory;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAAccessFactory;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmMapping;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmModelView;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAContextImpl;
import org.apache.olingo.odata2.jpa.processor.core.ODataJPAResponseBuilderDefault;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAProcessorImpl;
import org.apache.olingo.odata2.jpa.processor.core.edm.ODataJPAEdmProvider;
import org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmModel;

public class ODataJPAFactoryImpl extends org.apache.olingo.odata2.jpa.processor.core.factory.ODataJPAFactoryImpl {
	
	public ODataJPAFactoryImpl() {
		
	}
	
	@Override
	public JPAAccessFactory getJPAAccessFactory() {
		return JPAAccessFactoryImpl.create();
	}

	@Override
	public ODataJPAAccessFactory getODataJPAAccessFactory() {
		return ODataJPAAccessFactoryImpl.create();
	}

	private static class JPAAccessFactoryImpl implements JPAAccessFactory {

		private static JPAAccessFactoryImpl factory = null;

		private JPAAccessFactoryImpl() {
		}

		@Override
		public JPAEdmModelView getJPAEdmModelView(final ODataJPAContext oDataJPAContext) {
			JPAEdmModelView view = null;

			view = new JPAEdmModel(oDataJPAContext);
			return view;
		}

		@Override
		public JPAProcessor getJPAProcessor(final ODataJPAContext oDataJPAContext) {
			JPAProcessor jpaProcessor = new JPAProcessorImpl(oDataJPAContext);

			return jpaProcessor;
		}

		private static JPAAccessFactoryImpl create() {
			if (factory == null) {
				return new JPAAccessFactoryImpl();
			} else {
				return factory;
			}
		}

		@Override
		public JPAEdmMappingModelAccess getJPAEdmMappingModelAccess(final ODataJPAContext oDataJPAContext) {
			JPAEdmMappingModelAccess mappingModelAccess = new StandardNavigationNamesJPAEdmMappingModelService(oDataJPAContext);

			return mappingModelAccess;
		}

		@Override
		public JPAEdmMapping getJPAEdmMappingInstance() {
			return new JPAEdmMappingImpl();
		}

	}

	private static class ODataJPAAccessFactoryImpl implements ODataJPAAccessFactory {

		private static ODataJPAAccessFactoryImpl factory = null;

		private ODataJPAAccessFactoryImpl() {
		}

		@Override
		public ODataSingleProcessor createODataProcessor(final ODataJPAContext oDataJPAContext) {
			return new ODataJPADefaultProcessor(oDataJPAContext) {
			};
		}

		@Override
		public EdmProvider createJPAEdmProvider(final ODataJPAContext oDataJPAContext) {
			return new ODataJPAEdmProvider(oDataJPAContext);
		}

		@Override
		public ODataJPAContext createODataJPAContext() {
			return new ODataJPAContextImpl();
		}

		private static ODataJPAAccessFactoryImpl create() {
			if (factory == null) {
				return new ODataJPAAccessFactoryImpl();
			} else {
				return factory;
			}
		}

		@Override
		public ODataJPAMessageService getODataJPAMessageService(final Locale locale) {
			return ODataJPAMessageServiceDefault.getInstance(locale);
		}

		@Override
		public ODataJPAResponseBuilder getODataJPAResponseBuilder(final ODataJPAContext oDataJPAContext) {
			return new ODataJPAResponseBuilderDefault(oDataJPAContext);
		}

	}
}
