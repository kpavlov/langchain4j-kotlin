package me.kpavlov.langchain4j.kotlin.service

import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import me.kpavlov.langchain4j.kotlin.Configuration
import me.kpavlov.langchain4j.kotlin.PromptContent
import me.kpavlov.langchain4j.kotlin.TemplateName
import me.kpavlov.langchain4j.kotlin.prompt.PromptTemplateSource
import me.kpavlov.langchain4j.kotlin.prompt.TemplateRenderer

/**
 * TemplateSystemMessageProvider is responsible for providing system messages based on templates.
 *
 * @property templateName The name of the template to use for generating system messages.
 * @property promptTemplateSource Source from which the prompt templates are fetched.
 * @property promptTemplateRenderer Renderer used to render the content with specific variables.
 */
public open class TemplateSystemMessageProvider(
    private val templateName: TemplateName,
    private val promptTemplateSource: PromptTemplateSource = Configuration.promptTemplateSource,
    private val promptTemplateRenderer: TemplateRenderer = Configuration.promptTemplateRenderer,
) : SystemMessageProvider {
    public open fun templateName(): TemplateName = templateName

    /**
     * Generates a system message using a template and the provided chat memory identifier.
     *
     * @param chatMemoryID Identifier for the chat memory used to generate the system message.
     * @return A rendered prompt content string based on the template and chat memory identifier,
     *         or `null` if the template is not found.
     */
    override fun getSystemMessage(chatMemoryID: ChatMemoryId): PromptContent? {
        val promptTemplate = promptTemplateSource.getTemplate(templateName())
        require(promptTemplate != null) {
            "Can't find SystemPrompt template with name=\"$templateName()\""
        }
        val content = promptTemplate.content()
        return promptTemplateRenderer.render(content, mapOf("chatMemoryID" to chatMemoryID))
    }
}
