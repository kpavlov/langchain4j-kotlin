# Prompt Templates Guide

Learn how to use prompt templates with LangChain4J's AiServices. This guide covers setup, configuration, and
customization.

## Create Your First Template

Place your prompt templates in the classpath:

System prompt template (path: `prompts/default-system-prompt.mustache`):

```mustache
You are helpful assistant using chatMemoryID={{chatMemoryID}}
```

User prompt template (path: `prompts/default-user-prompt.mustache`):

```mustache
Hello, {{userName}}! {{message}}
```

## Quick Start

Here's how to use templates in your code:

```kotlin
// Define your assistant
interface Assistant {
  @UserMessage("prompts/default-user-prompt.mustache")
  fun askQuestion(
    @UserName userName: String, // Compile with javac `parameters=true` 
    @V("message") question: String,
  ): String
}

// Set up the assistant
val assistant: Assistant =
  AiServices
    .builder(Assistant::class.java)
    .systemMessageProvider(
      TemplateSystemMessageProvider("prompts/default-system-prompt.mustache")
    ).chatModel(model)
    .build()

// Use it
val response = assistant.askQuestion(
  userName = "My friend",
  question = "How are you?"
)
```

This creates:

- System prompt: "You are helpful assistant using chatMemoryID=default"
- User prompt: "Hello, My friend! How are you?"

## Under the Hood

Key components:

- `PromptTemplateFactory`: Gets templates and handles defaults
- `ClasspathPromptTemplateSource`: Loads templates from your classpath
- `SimpleTemplateRenderer`: Replaces `{{key}}` placeholders with values
- `RenderablePromptTemplate`: Connects everything together

## Customize Your Setup

Configure templates in `langchain4j-kotlin.properties`:

| Setting                    | Purpose                   | Default                         |
|----------------------------|---------------------------|---------------------------------|
| `prompt.template.source`   | Where templates load from | `ClasspathPromptTemplateSource` |
| `prompt.template.renderer` | How templates render      | `SimpleTemplateRenderer`        |

### Add Custom Template Sources

Create your own source by implementing `PromptTemplateSource`:

```kotlin
interface PromptTemplateSource {
  fun getTemplate(name: TemplateName): PromptTemplate?
}
```

Example using Redis:

```kotlin
class RedisPromptTemplateSource(private val jedis: Jedis) : PromptTemplateSource {
  override fun getTemplate(name: TemplateName): PromptTemplate? {
    return jedis.get(name)?.let {
      SimplePromptTemplate(it)
    }
  }
}
```

Enable it in your properties:

```properties
prompt.template.source=com.example.RedisPromptTemplateSource
```

### Create Custom Renderers

Build your own renderer:

```kotlin
interface TemplateRenderer {
  fun render(
    template: TemplateContent,
    variables: Map<String, Any?>
  ): String
}
```

Example:

```kotlin
class MyTemplateRenderer : TemplateRenderer {
  override fun render(template: TemplateContent, variables: Map<String, Any?>): String {
    TODO("Add implementation here")
  }
}
```

Enable it:

```properties
prompt.template.renderer=com.example.MyTemplateRenderer
```

## Learn More

Find complete examples:

- [Unit test example](../langchain4j-kotlin/src/test/kotlin/me/kpavlov/langchain4j/kotlin/service/ServiceWithPromptTemplatesTest.kt)
- [Using from Java](../samples/src/main/java/me/kpavlov/langchain4j/kotlin/samples/ServiceWithTemplateSourceJavaExample.java)

