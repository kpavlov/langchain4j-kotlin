package me.kpavlov.langchain4j.kotlin.model.chat

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder
import me.kpavlov.langchain4j.kotlin.TestEnvironment

internal fun createOpenAiStreamingModel(
    configurer: OpenAiStreamingChatModelBuilder.() -> Unit = {},
): StreamingChatModel {
    val modelBuilder =
        OpenAiStreamingChatModel
            .builder()
            .modelName("gpt-4o-mini")
            .temperature(0.1)
            .maxTokens(100)

    val apiKey = TestEnvironment["OPENAI_API_KEY"]
    if (apiKey != null) {
        modelBuilder.apiKey(apiKey)
    } else {
        modelBuilder
            .apiKey("my-key")
            .baseUrl("http://localhost:${TestEnvironment.mockOpenAi.port()}/v1")
    }
    configurer.invoke(modelBuilder)

    return modelBuilder.build()
}
