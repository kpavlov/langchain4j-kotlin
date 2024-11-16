package me.kpavlov.langchain4j.kotlin.prompt

interface PromptTemplate : dev.langchain4j.spi.prompt.PromptTemplateFactory.Template {
    fun content(): String
}
