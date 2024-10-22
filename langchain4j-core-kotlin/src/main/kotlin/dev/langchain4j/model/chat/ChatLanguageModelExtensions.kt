package dev.langchain4j.model.chat

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.coroutineScope

suspend fun ChatLanguageModel.generateAsync(userMessage: String): Result<String> {
    val model = this
    return coroutineScope { runCatching { model.generate(userMessage) } }
}

suspend fun ChatLanguageModel.generateAsync(messages: List<ChatMessage>): Result<Response<AiMessage>> {
    val model = this
    return coroutineScope { runCatching { model.generate(messages) } }
}


