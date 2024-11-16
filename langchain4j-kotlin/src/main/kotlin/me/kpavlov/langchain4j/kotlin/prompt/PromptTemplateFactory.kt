package me.kpavlov.langchain4j.kotlin.prompt

import dev.langchain4j.spi.prompt.PromptTemplateFactory
import me.kpavlov.langchain4j.kotlin.Configuration
import me.kpavlov.langchain4j.kotlin.internal.SENSITIVE

public open class PromptTemplateFactory : PromptTemplateFactory {
    protected val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    private val source: PromptTemplateSource = Configuration.promptTemplateSource
    private val renderer: TemplateRenderer = Configuration.promptTemplateRenderer

    override fun create(input: PromptTemplateFactory.Input): PromptTemplateFactory.Template {
        logger.info(
            "Create PromptTemplate input.template = ${input.template}, input.name = ${input.name}",
        )
        val template = source.getTemplate(input.template)
        return if (template == null) {
            if (logger.isTraceEnabled) {
                logger.trace(
                    SENSITIVE,
                    "Prompt template not found, failing back to input.template=\"{}\"",
                    input.template,
                )
            } else {
                logger.debug(
                    "Prompt template not found, failing back to input.template",
                )
            }
            RenderablePromptTemplate(
                name = input.name,
                content = input.template,
                templateRenderer = renderer,
            )
        } else {
            if (logger.isTraceEnabled) {
                logger.trace(
                    "Found Prompt template by name=\"{}\": \"{}\"",
                    input.template,
                    template,
                )
            } else {
                logger.debug(
                    "Found Prompt template by name=\"{}\"",
                    input.template,
                )
            }
            RenderablePromptTemplate(
                name = input.template,
                content = template.content(),
                templateRenderer = renderer,
            )
        }
    }
}
