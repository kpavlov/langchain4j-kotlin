# LangChain4j-Kotlin

[![Maven Central](https://img.shields.io/maven-central/v/me.kpavlov.langchain4j.kotlin/langchain4j-kotlin)](https://repo1.maven.org/maven2/me/kpavlov/langchain4j/kotlin/langchain4j-kotlin/)
[![Kotlin CI with Maven](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![codecov](https://codecov.io/gh/kpavlov/langchain4j-kotlin/graph/badge.svg?token=VYIJ92CYHD)](https://codecov.io/gh/kpavlov/langchain4j-kotlin)

[![Kotlin](https://img.shields.io/badge/langchain4j-1.1.0-green.svg)](https://docs.langchain4j.dev)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Api Docs](https://img.shields.io/badge/api-docs-blue)](https://kpavlov.github.io/langchain4j-kotlin/api/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/kpavlov/langchain4j-kotlin)

Kotlin enhancements for [LangChain4j](https://github.com/langchain4j/langchain4j), providing coroutine support and Flow-based streaming capabilities for chat language models.

See the [discussion](https://github.com/langchain4j/langchain4j/discussions/1897) on LangChain4j project.

> ℹ️ This project is a playground for [LangChain4j's Kotlin API](https://docs.langchain4j.dev/tutorials/kotlin). If
> accepted, some code might be adopted into the original [LangChain4j](https://github.com/langchain4j) project and removed
> from here. Mean while, enjoy it here.

## Features

- ✨ [Kotlin Coroutine](https://kotlinlang.org/docs/coroutines-guide.html) support for [ChatLanguageModels](https://docs.langchain4j.dev/tutorials/chat-and-language-models)
- 🌊 [Kotlin Asynchronous Flow](https://kotlinlang.org/docs/flow.html) support for [StreamingChatLanguageModels](https://docs.langchain4j.dev/tutorials/ai-services#streaming)
- 💄[External Prompt Templates](docs/PromptTemplates.md) support. Basic implementation loads both system and user prompt
  templates from the classpath,
  but [PromptTemplateSource](langchain4j-kotlin/src/main/kotlin/me/kpavlov/langchain4j/kotlin/prompt/PromptTemplateSource.kt)
  provides extension mechanism.
- 💾[Async Document Processing Extensions](docs/AsyncIO.md) support parallel document processing with Kotlin coroutines
  for efficient I/O operations in LangChain4j

See [api docs](https://kpavlov.github.io/langchain4j-kotlin/api/) for more details.

## Installation

### Maven

Add the following dependencies to your `pom.xml`:

```xml pom.xml

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-bom</artifactId>
      <version>2.2.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <dependency>
      <groupId>dev.langchain4j</groupId>
      <artifactId>langchain4j-bom</artifactId>
      <version>1.1.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
    <!-- LangChain4j Kotlin Extensions -->
    <dependency>
        <groupId>me.kpavlov.langchain4j.kotlin</groupId>
        <artifactId>langchain4j-kotlin</artifactId>
        <version>[LATEST_VERSION]</version>
    </dependency>
    
    <!-- Extra Dependencies -->
    <dependency>
      <groupId>dev.langchain4j</groupId>
      <artifactId>langchain4j</artifactId>
    </dependency>
    <dependency>
      <groupId>dev.langchain4j</groupId>
      <artifactId>langchain4j-open-ai</artifactId>
    </dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("me.kpavlov.langchain4j.kotlin:langchain4j-kotlin:$LATEST_VERSION")
  implementation("dev.langchain4j:langchain4j-open-ai:1.1.0")
}
```

## Quick Start

### Basic Chat Request

Extension can convert [`ChatModel`](https://docs.langchain4j.dev/tutorials/chat-and-language-models) response
into [Kotlin Suspending Function](https://kotlinlang.org/docs/coroutines-basics.html):

```kotlin
val model: ChatModel = OpenAiChatModel.builder()
    .apiKey("your-api-key")
    // more configuration parameters here ...
    .build()

// sync call
val response =
  model.chat(chatRequest {
    messages += systemMessage("You are a helpful assistant")
    messages += userMessage("Hello!")
  })
println(response.aiMessage().text())

// Using coroutines
CoroutineScope(Dispatchers.IO).launch {
    val response =
      model.chatAsync {
        messages += systemMessage("You are a helpful assistant")
        messages += userMessage("Say Hello")
        parameters(OpenAiChatRequestParameters.builder()) {
          temperature = 0.1
          builder.seed(42) // OpenAI specific parameter
        }
      }
    println(response.aiMessage().text())
}      
```

Sample code:

- [ChatModelExample.kt](samples/src/main/kotlin/me/kpavlov/langchain4j/kotlin/samples/ChatModelExample.kt)
- [OpenAiChatModelExample.kt](samples/src/main/kotlin/me/kpavlov/langchain4j/kotlin/samples/OpenAiChatModelExample.kt)

### Streaming Chat Language Model support

Extension can convert [StreamingChatModel](https://docs.langchain4j.dev/tutorials/response-streaming) response
into [Kotlin Asynchronous Flow](https://kotlinlang.org/docs/flow.html):

```kotlin
val model: StreamingChatModel = OpenAiStreamingChatModel.builder()
    .apiKey("your-api-key")
    // more configuration parameters here ...
    .build()

model.chatFlow {
  messages += systemMessage("You are a helpful assistant")
  messages += userMessage("Hello!")
}.collect { reply ->
    when (reply) {
      is CompleteResponse ->
            println(
                "Final response: ${reply.response.content().text()}",
            )

      is PartialResponse -> println("Received token: ${reply.token}")
        else -> throw IllegalArgumentException("Unsupported event: $reply")
    }
}
```

### Async AI Services

The library adds support for coroutine-based async AI services through the `AsyncAiServices` class, which leverages
Kotlin's coroutines for efficient asynchronous operations:

```kotlin
// Define your service interface with suspending function
interface Assistant {
  @UserMessage("Hello, my name is {{name}}. {{question}}")
  suspend fun chat(name: String, question: String): String
}

// Create the service using AsyncAiServicesFactory
val assistant = createAiService(
  serviceClass = Assistant::class.java,
  factory = AsyncAiServicesFactory(),
).chatModel(model)
  .build()

// Use with coroutines
runBlocking {
  val response = assistant.chat("John", "What is Kotlin?")
  println(response)
}
```

#### Advanced Usage Scenarios

The `AsyncAiServices` implementation uses `HybridVirtualThreadInvocationHandler` under the hood,
which supports multiple invocation patterns:

1. **Suspend Functions**: Native Kotlin coroutines support
2. **CompletionStage/CompletableFuture**: For Java-style async operations
3. **Blocking Operations**: Automatically run on virtual threads (Java 21+)

Example with different return types:

```kotlin
interface AdvancedAssistant {
  // Suspend function
  @UserMessage("Summarize: {{text}}")
  suspend fun summarize(text: String): String

  // CompletionStage return type for Java interoperability
  @UserMessage("Analyze sentiment: {{text}}")
  fun analyzeSentiment(text: String): CompletionStage<String>

  // Blocking operation (runs on virtual thread)
  @Blocking
  @UserMessage("Process document: {{document}}")
  fun processDocument(document: String): String
}
```

#### Benefits

- **Efficient Resource Usage**: Suspending functions don't block threads during I/O or waiting
- **Java Interoperability**: Support for CompletionStage/CompletableFuture return types
- **Virtual Thread Integration**: Automatic handling of blocking operations on virtual threads
- **Simplified Error Handling**: Leverage Kotlin's structured concurrency for error propagation
- **Reduced Boilerplate**: No need for manual callback handling or future chaining

### Kotlin Notebook

The [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html) environment allows you to:

* Experiment with LLM features in real-time
* Test different configurations and scenarios
* Visualize results directly in the notebook
* Share reproducible examples with others

You can easily get started with LangChain4j-Kotlin notebooks:

```kotlin
%useLatestDescriptors
%use coroutines

@file:DependsOn("dev.langchain4j:langchain4j:0.36.2")
@file:DependsOn("dev.langchain4j:langchain4j-open-ai:0.36.2")

// add maven dependency
@file:DependsOn("me.kpavlov.langchain4j.kotlin:langchain4j-kotlin:0.1.1")
// ... or add project's target/classes to classpath
//@file:DependsOn("../target/classes")

import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import me.kpavlov.langchain4j.kotlin.model.chat.chatAsync
  
val model = OpenAiChatModel.builder()
  .apiKey("demo")
  .modelName("gpt-4o-mini")
  .temperature(0.0)
  .maxTokens(1024)
  .build()

// Invoke using CoroutineScope
val scope = CoroutineScope(Dispatchers.IO)

runBlocking {
  val result = model.chatAsync {
    messages += systemMessage("You are helpful assistant")
    messages += userMessage("Make a haiku about Kotlin, Langchain4j and LLM")
  }
  println(result.content().text())
}
```

Try [this Kotlin Notebook](langchain4j-kotlin/notebooks/lc4kNotebook.ipynb) yourself:
![](docs/kotlin-notebook-1.png)

## Development Setup

### Prerequisites

1. Create `.env` file in root directory and add your API keys:

```dotenv
OPENAI_API_KEY=sk-xxxxx
```

### Building the Project

Using Maven:

```shell
mvn clean verify
```

Using Make:

```shell
make build
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

Run before submitting your changes

```shell
make lint
```

## Acknowledgements

- [LangChain4j](https://github.com/langchain4j/langchain4j) - The core library this project enhances
- Training data from Project Gutenberg:
  - [CAPTAIN BLOOD By Rafael Sabatini](https://www.gutenberg.org/cache/epub/1965/pg1965.txt)

## License

[MIT License](LICENSE.txt)
