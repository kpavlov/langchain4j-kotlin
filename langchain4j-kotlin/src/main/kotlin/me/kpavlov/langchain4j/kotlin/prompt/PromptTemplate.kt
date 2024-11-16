package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent

public interface PromptTemplate {
    fun content(): String
}

public data class SimplePromptTemplate(
    private val content: TemplateContent,
) : PromptTemplate {
    override fun content(): TemplateContent = content
}
