package de.incentergy.architecture.entities;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TodoTest {
    @Test
    public void testSetId() {
        Todo todo = new Todo();
        todo.setId("id");
        assertEquals("id", todo.getId());
    }
}
