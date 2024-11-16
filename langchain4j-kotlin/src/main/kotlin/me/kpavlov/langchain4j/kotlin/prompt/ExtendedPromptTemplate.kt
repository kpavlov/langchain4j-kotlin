package me.kpavlov.langchain4j.kotlin.prompt

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ExtendedPromptTemplate::class.java)

class ExtendedPromptTemplate(
    val name: String,
    private val content: String,
    private val templateRenderer: TemplateRenderer = SimpleTemplateRenderer,
) : PromptTemplate {
    override fun content(): String = content

    override fun render(variables: Map<String, Any>): String {
        logger.info("Rendering template: {}", name)
        return templateRenderer.render(template = content, variables = variables)
    }
}
