package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent

/**
 * Interface representing a template for prompts.
 *
 * Implementations of this interface provide the template content that can be used
 * to generate prompts. The content is expected to be a string format that can
 * incorporate variables or placeholders.
 */
public interface PromptTemplate {
    fun content(): TemplateContent
}

/**
 * Data class representing a simple implementation of the [PromptTemplate] interface.
 *
 * This class provides a concrete implementation of the [PromptTemplate] interface by
 * storing the template content and returning it via the `content` method. It is
 * designed to work with prompt templates loaded from various sources.
 *
 * @param content The content of the template, represented as [TemplateContent].
 */
public data class SimplePromptTemplate(
    private val content: TemplateContent,
) : PromptTemplate {
    override fun content(): TemplateContent = content
}
