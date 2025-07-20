package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import me.kpavlov.finchly.BaseTestEnvironment

val testEnv = BaseTestEnvironment()

val model: ChatModel =
    OpenAiChatModel
        .builder()
        .modelName("gpt-4o-nano")
        .apiKey(testEnv["OPENAI_API_KEY"])
        .build()
