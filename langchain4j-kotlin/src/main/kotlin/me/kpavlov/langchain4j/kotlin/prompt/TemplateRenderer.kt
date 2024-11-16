package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent

/**
 * Interface for rendering a text template with provided variables.
 *
 * Implementers of this interface will typically replace placeholders in the template
 * with corresponding values from the variables map.
 */
public interface TemplateRenderer {
    public fun render(
        template: TemplateContent,
        variables: Map<String, Any?>,
    ): String
}
