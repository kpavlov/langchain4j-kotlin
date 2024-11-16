package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateName

/**
 * Interface for obtaining prompt templates by their name.
 *
 * This interface defines a method for retrieving a prompt template using
 * a template name. The implementation of this interface will determine
 * how and from where the templates are sourced.
 */
interface PromptTemplateSource {
    /**
     * Retrieves a prompt template based on the provided template name.
     *
     * @param name The name of the template to retrieve.
     * @return The prompt template associated with the specified name, or null if no such template exists.
     */
    fun getTemplate(name: TemplateName): PromptTemplate?
}
