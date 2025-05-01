package me.kpavlov.langchain4j.kotlin.prompt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SimpleTemplateRendererTest {
    private val subject = SimpleTemplateRenderer()

    @Test
    fun `render returns original string when no placeholders are present`() {
        val template = "No placeholders here"
        val variables: Map<String, Any?> = mapOf("key" to "value")

        val result = subject.render(template, variables)

        assertEquals(template, result)
    }

    @Test
    fun `render replaces placeholders with variable values`() {
        val template = "Hello, {{name}}"
        val variables: Map<String, Any?> = mapOf("name" to "John")

        val result = subject.render(template, variables)

        assertEquals("Hello, John", result)
    }

    @Test
    fun `render replaces multiple placeholders with variable values`() {
        val template = "Hello, {{name}}, you are {{age}} years old."
        val variables: Map<String, Any?> = mapOf("name" to "John", "age" to 47)

        val result = subject.render(template, variables)

        assertEquals("Hello, John, you are 47 years old.", result)
    }

    @Test
    fun `render replaces undefined placeholders with an empty string`() {
        val template = "Hello, {{customer}}! My name is {{agent}}."
        val variables: Map<String, Any?> = mapOf()

        val exception =
            assertThrows<IllegalArgumentException> {
                subject.render(
                    template,
                    variables,
                )
            }
        assertEquals("Undefined keys in template: customer, agent", exception.message)
    }
}
