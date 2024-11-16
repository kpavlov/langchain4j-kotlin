package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent

public interface TemplateRenderer {
    public fun render(
        template: TemplateContent,
        variables: Map<String, Any?>,
    ): String
}
