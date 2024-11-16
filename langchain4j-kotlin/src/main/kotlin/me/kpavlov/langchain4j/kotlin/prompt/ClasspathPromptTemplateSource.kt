package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateName

/**
 * Classpath-based implementation of [PromptTemplateSource].
 *
 * This class provides a mechanism to load prompt templates from the classpath
 * using the template name as the resource identifier. It attempts to locate the
 * template file in the classpath and reads its contents as the template data.
 */
public open class ClasspathPromptTemplateSource : PromptTemplateSource {
    /**
     * Retrieves a prompt template based on the provided template name.
     *
     * This method attempts to locate the template file in the classpath using the given
     * template name as the resource identifier. If found, it reads the contents of the
     * file and returns a [SimplePromptTemplate] containing the template data.
     *
     * @param name The name of the template to retrieve.
     * @return The prompt template associated with the specified name, or null if no such template exists.
     */
    override fun getTemplate(name: TemplateName): PromptTemplate? {
        val resourceStream = this::class.java.classLoader.getResourceAsStream(name)
        return resourceStream?.bufferedReader()?.use { reader ->
            val content = reader.readText()
            return SimplePromptTemplate(content)
        }
    }
}
