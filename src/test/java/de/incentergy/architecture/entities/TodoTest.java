package de.incentergy.architecture.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TodoTest {
    @Test
    public void testSetId() {
        Todo todo = new Todo();
        todo.setId("id");
        assertEquals("id", todo.getId());
    }
}
