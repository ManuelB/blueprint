package de.incentergy.architecture;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPATransaction;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;

public class JpaODataServiceFactory extends ODataJPAServiceFactory {

	private static Logger log = Logger.getLogger(JpaODataServiceFactory.class.getName());

	ODataJPAContext oDataJPAContext;

	private ODataJPAFactory oDataJPAFactory = new ODataJPAFactoryImpl();

	static {
		Class<?> classUtil;
		try {
			classUtil = ODataJPAFactory.class;
			
			// https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
			Field implementationField = classUtil.getDeclaredField("factoryImpl");
			implementationField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(implementationField, implementationField.getModifiers() & ~Modifier.FINAL);
			
			// Reset the cache
			implementationField.set(null, new ODataJPAFactoryImpl());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			log.log(Level.WARNING, "Problem with modifying Olingo private final variable", e1);
		}
	}
	
	@Override
	public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {

		oDataJPAContext = getMyODataJPAContext();
		setDetailErrors(true);

		InitialContext initialContext;
		try {
			initialContext = new InitialContext();
			// BeanManager beanManager = (BeanManager)
			// initialContext.lookup("java:comp/BeanManager");
			EntityManager entityManager = (EntityManager) initialContext.lookup("java:/myEntityManager");
			oDataJPAContext.setEntityManager(entityManager);
			EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initialContext
					.lookup("java:/myEntityManagerFactory");
			oDataJPAContext.setEntityManagerFactory(entityManagerFactory);
			oDataJPAContext.setPersistenceUnitName("blueprint");
			oDataJPAContext.setContainerManaged(true);
			final UserTransaction userTransaction = (UserTransaction) initialContext
					.lookup("java:comp/UserTransaction");
			setODataJPATransaction(new ODataJPATransaction() {

				@Override
				public void rollback() {
					try {
						userTransaction.rollback();
					} catch (IllegalStateException | SecurityException | SystemException e) {
						log.log(Level.SEVERE, "Problem with rollback", e);
					}
				}

				@Override
				public boolean isActive() {
					try {
						return userTransaction.getStatus() == Status.STATUS_ACTIVE;
					} catch (SystemException e) {
						log.log(Level.SEVERE, "Problem with isActive", e);
						return false;
					}
				}

				@Override
				public void commit() {
					try {
						userTransaction.commit();
					} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
							| HeuristicRollbackException | SystemException e) {
						log.log(Level.SEVERE, "Problem with commit", e);
					}
				}

				@Override
				public void begin() {
					try {
						userTransaction.begin();
					} catch (NotSupportedException | SystemException e) {
						log.log(Level.SEVERE, "Problem with begin", e);
					}
				}
			});
			return oDataJPAContext;
		} catch (NamingException e) {
			throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.ENTITY_MANAGER_NOT_INITIALIZED, e);
		}
	}

	/**
	 * @return an instance of type {@link ODataJPAContext}
	 * @throws ODataJPARuntimeException
	 */
	public final ODataJPAContext getMyODataJPAContext() throws ODataJPARuntimeException {
		if (oDataJPAContext == null) {
			oDataJPAContext = oDataJPAFactory.getODataJPAAccessFactory().createODataJPAContext();
		}
		return oDataJPAContext;
	}

}
