package me.kpavlov.langchain4j.kotlin.prompt

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RenderablePromptTemplate::class.java)

class RenderablePromptTemplate(
    val name: String,
    private val content: String,
    private val templateRenderer: TemplateRenderer,
) : PromptTemplate,
    dev.langchain4j.spi.prompt.PromptTemplateFactory.Template {
    override fun content(): String = content

    override fun render(variables: Map<String, Any>): String {
        logger.info("Rendering template: {}", name)
        return templateRenderer.render(template = content, variables = variables)
    }
}
