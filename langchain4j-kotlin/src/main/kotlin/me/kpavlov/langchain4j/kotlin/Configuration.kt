package me.kpavlov.langchain4j.kotlin

import java.util.Properties

object Configuration {
    val properties: Properties = loadProperties()
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
