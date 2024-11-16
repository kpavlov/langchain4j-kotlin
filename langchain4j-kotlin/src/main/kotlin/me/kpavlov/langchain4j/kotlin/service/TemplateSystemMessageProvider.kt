package me.kpavlov.langchain4j.kotlin.service

import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import me.kpavlov.langchain4j.kotlin.Configuration
import me.kpavlov.langchain4j.kotlin.prompt.PromptTemplateSource
import me.kpavlov.langchain4j.kotlin.prompt.TemplateRenderer

public open class TemplateSystemMessageProvider(
    private val templateName: String,
    private val promptTemplateSource: PromptTemplateSource = Configuration.promptTemplateSource,
    private val promptTemplateRenderer: TemplateRenderer = Configuration.promptTemplateRenderer,
) : SystemMessageProvider {
    public open fun templateName(): String = templateName

    override fun getSystemMessage(chatMemoryID: ChatMemoryId): String? {
        val promptTemplate = promptTemplateSource.getTemplate(templateName())
        require(promptTemplate != null) {
            "Can't find SystemPrompt template with name=\"$templateName()\""
        }
        val content = promptTemplate.content()
        return promptTemplateRenderer.render(content, mapOf("chatMemoryID" to chatMemoryID))
    }
}
