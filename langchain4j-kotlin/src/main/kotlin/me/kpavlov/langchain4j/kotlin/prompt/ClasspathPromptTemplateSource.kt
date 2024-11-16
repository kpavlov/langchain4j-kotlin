package me.kpavlov.langchain4j.kotlin.prompt

class ClasspathPromptTemplateSource : PromptTemplateSource {
    override fun getTemplate(name: String): PromptTemplate? {
        val resourceStream = this::class.java.classLoader.getResourceAsStream(name)
        return resourceStream?.bufferedReader()?.use { reader ->
            val content = reader.readText()
            return SimplePromptTemplate(content)
        }
    }
}
