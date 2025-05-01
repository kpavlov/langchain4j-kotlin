package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent
import me.kpavlov.langchain4j.kotlin.TemplateName
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RenderablePromptTemplate::class.java)

/**
 * Represents a renderable template for prompts.
 *
 * This class implements both [PromptTemplate] and LC4J's `PromptTemplateFactory.Template` interfaces.
 * It uses a [TemplateRenderer] to render the template content using provided variables.
 *
 * @property name The name of the template.
 * @property content The content of the template.
 * @property templateRenderer The renderer used for generating the final template string from the content and variables.
 */
public class RenderablePromptTemplate(
    public val name: TemplateName,
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
