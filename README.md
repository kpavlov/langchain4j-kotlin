# LangChain4j-Kotlin

[![Maven Central](https://img.shields.io/maven-central/v/me.kpavlov.langchain4j.kotlin/langchain4j-kotlin)](https://repo1.maven.org/maven2/me/kpavlov/langchain4j/kotlin/langchain4j-kotlin/)
[![Kotlin CI with Maven](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![Maintainability](https://api.codeclimate.com/v1/badges/176ba2c4e657d3e7981a/maintainability)](https://codeclimate.com/github/kpavlov/langchain4j-kotlin/maintainability)

Kotlin enhancements for [LangChain4j](https://github.com/langchain4j/langchain4j), providing coroutine support and Flow-based streaming capabilities for chat language models.

See the [discussion](https://github.com/langchain4j/langchain4j/discussions/1897) on LangChain4j project.

<p style="background-color: powderblue; padding: 10px; border-radius: 10px">
ℹ️ I am verifying my ideas for improving LangChain4j here. 
If an idea is accepted, the code might be adopted into the original [LangChain4j](https://github.com/langchain4j) project. If not - you may enjoy it here.
</p>

## Features

- ✨ [Kotlin Coroutine](https://kotlinlang.org/docs/coroutines-guide.html) support for [ChatLanguageModels](https://docs.langchain4j.dev/tutorials/chat-and-language-models)
- 🌊 [Kotlin Asynchronous Flow](https://kotlinlang.org/docs/flow.html) support for [StreamingChatLanguageModels](https://docs.langchain4j.dev/tutorials/ai-services#streaming)
- 💄[External Prompt Templates](docs/PromptTemplates.md) support. Basic implementation loads both system and user prompt
  templates from the classpath,
  but [PromptTemplateSource](langchain4j-kotlin/src/main/kotlin/me/kpavlov/langchain4j/kotlin/prompt/PromptTemplateSource.kt)
  provides extension mechanism.

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
      <version>0.36.0</version>
    </dependency>
    <dependency>
         <groupId>dev.langchain4j</groupId>
         <artifactId>langchain4j-open-ai</artifactId>
        <version>0.36.0</version>
    </dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("me.kpavlov.langchain4j.kotlin:langchain4j-kotlin:$LATEST_VERSION")
    implementation("dev.langchain4j:langchain4j-open-ai:0.36.0")
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
    model.chat(
        ChatRequest
            .builder()
            .messages(
                listOf(
                    SystemMessage.from("You are a helpful assistant"),
                    UserMessage.from("Hello!"),
                ),
            ).build(),
    )
println(response.aiMessage().text())

// Using coroutines
CoroutineScope(Dispatchers.IO).launch {
    val response =
        model.chatAsync(
            ChatRequest
                .builder()
                .messages(
                    listOf(
                        SystemMessage.from("You are a helpful assistant"),
                        UserMessage.from("Hello!"),
                    ),
                ),
        )
    println(response.aiMessage().text())
}      
```

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

@file:DependsOn("dev.langchain4j:langchain4j:0.36.0")
@file:DependsOn("dev.langchain4j:langchain4j-open-ai:0.36.0")

// add maven dependency
@file:DependsOn("me.kpavlov.langchain4j.kotlin:langchain4j-kotlin:0.1.1")
// ... or add project's target/classes to classpath
//@file:DependsOn("../target/classes")

import dev.langchain4j.data.message.*
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
      SystemMessage.from("You are helpful assistant"),
      UserMessage.from("Make a haiku about Kotlin, Langchani4j and LLM"),
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
