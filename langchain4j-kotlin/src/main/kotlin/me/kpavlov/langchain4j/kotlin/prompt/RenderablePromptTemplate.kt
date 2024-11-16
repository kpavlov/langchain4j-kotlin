package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent
import me.kpavlov.langchain4j.kotlin.TemplateName
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RenderablePromptTemplate::class.java)

class RenderablePromptTemplate(
    val name: TemplateName,
    private val content: TemplateContent,
    private val templateRenderer: TemplateRenderer,
) : PromptTemplate,
    dev.langchain4j.spi.prompt.PromptTemplateFactory.Template {
    override fun content(): TemplateContent = content

    override fun render(variables: Map<String, Any>): String {
        logger.info("Rendering template: {}", name)
        return templateRenderer.render(template = content, variables = variables)
    }
}
