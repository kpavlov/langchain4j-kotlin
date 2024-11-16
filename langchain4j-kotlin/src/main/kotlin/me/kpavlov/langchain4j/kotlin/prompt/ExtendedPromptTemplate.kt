package me.kpavlov.langchain4j.kotlin.prompt

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ExtendedPromptTemplate::class.java)

class ExtendedPromptTemplate(
    val name: String,
    val content: String,
    private val templateRenderer: TemplateRenderer = SimpleTemplateRenderer,
) : dev.langchain4j.spi.prompt.PromptTemplateFactory.Template {
    override fun render(variables: Map<String, Any>): String {
        logger.info("Rendering template: {}", name)
        return templateRenderer.render(template = content, variables = variables)
    }
}
