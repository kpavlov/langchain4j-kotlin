package me.kpavlov.langchain4j.kotlin

import me.kpavlov.langchain4j.kotlin.prompt.PromptTemplateSource
import me.kpavlov.langchain4j.kotlin.prompt.TemplateRenderer
import java.util.Properties

object Configuration {
    val properties: Properties = loadProperties()

    operator fun get(key: String): String = properties.getProperty(key)

    val promptTemplateSource: PromptTemplateSource =
        createInstanceByName(this["prompt.template.source"])
    val promptTemplateRenderer: TemplateRenderer =
        createInstanceByName(this["prompt.template.renderer"])
}

private fun loadProperties(fileName: String = "langchain4j-kotlin.properties"): Properties {
    val properties = Properties()
    val classLoader = Thread.currentThread().contextClassLoader
    classLoader.getResourceAsStream(fileName).use { inputStream ->
        require(inputStream != null) {
            "Property file '$fileName' not found in the classpath"
        }
        properties.load(inputStream)
    }
    return properties
}

@Suppress("UNCHECKED_CAST", "TooGenericExceptionCaught")
private fun <T> createInstanceByName(className: String): T =
    try {
        // Get the class object by name
        val clazz = Class.forName(className)
        // Create an instance of the class
        clazz.getDeclaredConstructor().newInstance() as T
    } catch (e: Exception) {
        throw IllegalArgumentException("Can't create $className", e)
    }