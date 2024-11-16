package me.kpavlov.langchain4j.kotlin.prompt

interface PromptTemplateSource {
    fun getTemplate(name: String): PromptTemplate?
}
