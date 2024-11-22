# Customizing Prompt Templates

This guide demonstrates how to configure and use prompt templates with
the [LangChain4J's AiServices](https://docs.langchain4j.dev/tutorials/ai-services). This setup involves configuring
prompt templates, defining and extending prompt template sources and template rendering.

## Creating and Using Prompt Template

Let's start with built-in mechanism of loading prompt template from classpath. Your prompt templates should be located
in the classpath, e.g.

File: `prompts/default-system-prompt.mustache`

```mustache
You are helpful assistant using chatMemoryID={{chatMemoryID}}
```

File: `prompts/default-user-prompt.mustache`

```mustache
Hello, {{userName}}! {{message}}
```

## Example with AiServices

Define an interface that uses these templates and configure the AiServices builder:

```kotlin
// Define assistant interface
interface Assistant {
  @UserMessage(
    // "Hello, {{userName}}! {{message}}"
    "prompts/default-user-prompt.mustache", //template name/resource
  )
  fun askQuestion(
    @UserName userName: String,
    @V("message") question: String,
  ): String
}

// Define Chain of Thoughts
val assistant: Assistant =
  AiServices
    .builder(Assistant::class.java)
    .systemMessageProvider(
      TemplateSystemMessageProvider(
        // "You are helpful assistant using chatMemoryID={{chatMemoryID}}"
        "prompts/default-system-prompt.mustache", // template name/recource
      ),
    ).chatLanguageModel(model)
    .build()

// Run it!
val response =
  assistant.askQuestion(
    userName = "My friend",
    question = "How are you?",
  )
```

System and user prompts will be:

- **System prompt:** "You are helpful assistant using chatMemoryID=default"
- **User Prompt:** "Hello, My friend! How are you?"

## How does it work

In the default implementation, `TemplateSystemMessageProvider` handles the system prompt template and `AiServices` uses
the templates to generate prompts.

`PromptTemplateFactory` provides `PromptTemplateFactory.Template` for `AiServices`. It is registered automatically via
Java ServiceLoaders mechanism. This class is responsible for obtaining prompt templates from a `PromptTemplateSource`.
If the specified template cannot be found, it will fallback to using default LC4J's the input template content.

`ClasspathPromptTemplateSource` is implementing `PromptTemplateSource` interface and provides a mechanism to load prompt
templates from the classpath using the template name as the resource identifier. It attempts to locate the template file
in the classpath and reads its contents as the template data. It is registered via property file and might be
overridden.

Implementers of the `TemplateRenderer` interface will typically replace placeholders in the template with corresponding
values from the variables map.

`SimpleTemplateRenderer` finds and replaces placeholders in the template in the Mustache-like format `{{key}}`, where
`key`
corresponds to an entry in the variables map. If any placeholders in the template are not defined in the variables map,
an `IllegalArgumentException` will be thrown.

`RenderablePromptTemplate` implements both `PromptTemplate` and LangChain4j's `PromptTemplateFactory.Template`
interfaces. It uses a `TemplateRenderer` to render the template content using provided variables.

## Customization

You may customize templates via configuration file `langchain4j-kotlin.properties`, located in the classpath.

| Key                        | Description       | Default Value                                                        |
|----------------------------|-------------------|----------------------------------------------------------------------|
| `prompt.template.source`   | Template source   | `me.kpavlov.langchain4j.kotlin.prompt.ClasspathPromptTemplateSource` |
| `prompt.template.renderer` | Template renderer | `me.kpavlov.langchain4j.kotlin.prompt.SimpleTemplateRenderer`        |

### Extending PromptTemplateSource

To create a custom template source, implement the PromptTemplateSource interface:

```kotlin
interface PromptTemplateSource {
  fun getTemplate(name: TemplateName): PromptTemplate?
}
```

Example implementation for Redis and Jedis:

```kotlin
package com.example

// Redis/Jedis-backed template source

class RedisPromptTemplateSource(private val jedis: Jedis) : PromptTemplateSource {
  override fun getTemplate(name: TemplateName): PromptTemplate? {
    return jedis.get(name)?.let {
      SimplePromptTemplate(it)
    }
  }
}
```

Register your implementation in the `langchain4j-kotlin.properties` configuration file:

```properties
prompt.template.source=com.example.RedisPromptTemplateSource
```

### Extending TemplateRenderer

To create a custom template renderer, implement the TemplateRenderer interface:

```kotlin
interface TemplateRenderer {
  fun render(
    template: TemplateContent,
    variables: Map<String, Any?>
  ): String
}
```

Example implementation:

```kotlin
package com.example

// Freemarker-based renderer
class MyTemplateRenderer : TemplateRenderer {

  override fun render(template: TemplateContent, variables: Map<String, Any?>): String {
    TODO("Add implementation here")
  }
}
```

Register your implementation in the `langchain4j-kotlin.properties` configuration file:

```properties
prompt.template.renderer=com.example.MyTemplateRenderer
```

## Examples

You may find the unit test with the
example [here](../langchain4j-kotlin/src/test/kotlin/me/kpavlov/langchain4j/kotlin/service/ServiceWithPromptTemplatesTest.kt)
