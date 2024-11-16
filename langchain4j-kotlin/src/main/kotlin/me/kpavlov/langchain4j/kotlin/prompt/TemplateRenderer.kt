package me.kpavlov.langchain4j.kotlin.prompt

public interface TemplateRenderer {
    public fun render(
        template: String,
        variables: Map<String, Any?>,
    ): String
}
