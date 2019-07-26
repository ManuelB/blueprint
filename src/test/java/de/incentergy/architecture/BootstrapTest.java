package de.incentergy.architecture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import de.incentergy.architecture.entities.Todo;

class BootstrapTest {

	@Test
	void testGenerateDemoData() {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.em = mock(EntityManager.class);
		bootstrap.generateDemoData();
		verify(bootstrap.em, times(1000)).persist(any(Todo.class));
	}

}
