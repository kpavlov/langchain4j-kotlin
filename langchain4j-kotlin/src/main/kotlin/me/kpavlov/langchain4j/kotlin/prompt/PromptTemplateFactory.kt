package me.kpavlov.langchain4j.kotlin.prompt

import dev.langchain4j.spi.prompt.PromptTemplateFactory
import me.kpavlov.langchain4j.kotlin.Configuration

class PromptTemplateFactory : PromptTemplateFactory {
    private val source: PromptTemplateSource = Configuration.promptTemplateSource
    private val renderer: TemplateRenderer = Configuration.promptTemplateRenderer

    init {
        Configuration.properties.getProperty("prompt.template.source")?.let {
            println("Found prompt template source: $it")
        }
        Configuration.properties.getProperty("prompt.template.renderer")?.let {
            println("Found prompt template source: $it")
        }
        println("PromptTemplateFactory initialized.")
    }

    override fun create(input: PromptTemplateFactory.Input): PromptTemplateFactory.Template {
        println(
            "Create PromptTemplate input.template = ${input.template}, input.name = ${input.name}",
        )
        val template = source.getTemplate(input.template)
        return if (template == null) {
            RenderablePromptTemplate(
                name = input.name,
                content = input.template,
                templateRenderer = renderer,
            )
        } else {
            RenderablePromptTemplate(
                name = input.template,
                content = template.content(),
                templateRenderer = renderer,
            )
        }
    }
}
