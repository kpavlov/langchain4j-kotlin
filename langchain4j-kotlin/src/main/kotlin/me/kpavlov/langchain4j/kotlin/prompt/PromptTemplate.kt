package me.kpavlov.langchain4j.kotlin.prompt

public interface PromptTemplate {
    fun content(): String
}

public data class SimplePromptTemplate(
    private val content: String,
) : PromptTemplate {
    override fun content(): String = content
}
