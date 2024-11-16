package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateName

interface PromptTemplateSource {
    fun getTemplate(name: TemplateName): PromptTemplate?
}
