package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateName

class ClasspathPromptTemplateSource : PromptTemplateSource {
    override fun getTemplate(name: TemplateName): PromptTemplate? {
        val resourceStream = this::class.java.classLoader.getResourceAsStream(name)
        return resourceStream?.bufferedReader()?.use { reader ->
            val content = reader.readText()
            return SimplePromptTemplate(content)
        }
    }
}
