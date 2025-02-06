# LangChain4j-Kotlin

[![Maven Central](https://img.shields.io/maven-central/v/me.kpavlov.langchain4j.kotlin/langchain4j-kotlin)](https://repo1.maven.org/maven2/me/kpavlov/langchain4j/kotlin/langchain4j-kotlin/)
[![Kotlin CI with Maven](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![codecov](https://codecov.io/gh/kpavlov/langchain4j-kotlin/graph/badge.svg?token=VYIJ92CYHD)](https://codecov.io/gh/kpavlov/langchain4j-kotlin)
[![Maintainability](https://api.codeclimate.com/v1/badges/176ba2c4e657d3e7981a/maintainability)](https://codeclimate.com/github/kpavlov/langchain4j-kotlin/maintainability)
[![Api Docs](https://img.shields.io/badge/api-docs-blue)](https://kpavlov.github.io/langchain4j-kotlin/api/)
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

```xml
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
      <version>1.0.0-beta1</version>
    </dependency>
    <dependency>
         <groupId>dev.langchain4j</groupId>
         <artifactId>langchain4j-open-ai</artifactId>
      <version>1.0.0-beta1</version>
    </dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("me.kpavlov.langchain4j.kotlin:langchain4j-kotlin:$LATEST_VERSION")
  implementation("dev.langchain4j:langchain4j-open-ai:1.0.0-beta1")
}
```

## Quick Start

### Basic Chat Request

Extension can convert [`ChatLanguageModel`](https://docs.langchain4j.dev/tutorials/chat-and-language-models) response into [Kotlin Suspending Function](https://kotlinlang.org/docs/coroutines-basics.html):

```kotlin
val model: ChatLanguageModel = OpenAiChatModel.builder()
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

Extension can convert [StreamingChatLanguageModel](https://docs.langchain4j.dev/tutorials/response-streaming) response into [Kotlin Asynchronous Flow](https://kotlinlang.org/docs/flow.html):

```kotlin
val model: StreamingChatLanguageModel = OpenAiStreamingChatModel.builder()
    .apiKey("your-api-key")
    // more configuration parameters here ...
    .build()

model.generateFlow(messages).collect { reply ->
    when (reply) {
        is Completion ->
            println(
                "Final response: ${reply.response.content().text()}",
            )

        is Token -> println("Received token: ${reply.token}")
        else -> throw IllegalArgumentException("Unsupported event: $reply")
    }
}
```

### Kotlin Notebook

The [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html) environment allows you to:

* Experiment with LLM features in real-time
* Test different configurations and scenarios
* Visualize results directly in the notebook
* Share reproducible examples with others

You can easyly get started with LangChain4j-Kotlin notebooks:

```kotlin notebook
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

import me.kpavlov.langchain4j.kotlin.model.chat.generateAsync

  
val model = OpenAiChatModel.builder()
  .apiKey("demo")
  .modelName("gpt-4o-mini")
  .temperature(0.0)
  .maxTokens(1024)
  .build()

// Invoke using CoroutineScope
val scope = CoroutineScope(Dispatchers.IO)

runBlocking {
  val result = model.generateAsync(
    listOf(
      systemMessage("You are helpful assistant"),
      userMessage("Make a haiku about Kotlin, Langchani4j and LLM"),
    )
  )
  println(result.content().text())
}
```

Try [this Kotlin Notebook](langchain4j-kotlin/notebooks/lc4kNotebook.ipynb)  yourself:
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
