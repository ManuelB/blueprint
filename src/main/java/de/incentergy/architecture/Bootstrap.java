package de.incentergy.architecture;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.incentergy.architecture.entities.Todo;

@Singleton
@Startup
public class Bootstrap {
	
	@PersistenceContext
	EntityManager em;

	@PostConstruct
	public void generateDemoData() {
		for(int i=0;i<1000;i++) {
			Todo todo = new Todo();
			todo.setNote(todo.getId());
			em.persist(todo);
		}
	}
}
