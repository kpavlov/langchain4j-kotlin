package me.kpavlov.langchain4j.kotlin.model.chat

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.coroutineScope
import me.kpavlov.langchain4j.kotlin.model.chat.request.ChatRequestBuilder
import me.kpavlov.langchain4j.kotlin.model.chat.request.chatRequest

/**
 * Asynchronously processes a chat request using the language model within
 * a coroutine scope. This extension function provides a structured
 * concurrency wrapper around the synchronous [ChatLanguageModel.chat] method.
 *
 * Example usage:
 * ```kotlin
 * val response = model.chatAsync(ChatRequest(messages))
 * ```
 *
 * @param request The chat request containing messages and optional parameters
 *    for the model.
 * @return [ChatResponse] containing the model's response and any additional
 *    metadata.
 * @throws Exception if the chat request fails or is interrupted.
 * @see ChatLanguageModel.chat
 * @see ChatRequest
 * @see ChatResponse
 */
suspend fun ChatLanguageModel.chatAsync(request: ChatRequest): ChatResponse {
    val model = this
    return coroutineScope { model.chat(request) }
}

/**
 * Asynchronously processes a chat request using a [ChatRequest.Builder] for
 * convenient request configuration. This extension function combines the
 * builder pattern with coroutine-based asynchronous execution.
 *
 * Example usage:
 * ```kotlin
 * val response = model.chatAsync(ChatRequest.builder()
 *     .messages(listOf(UserMessage("Hello")))
 *     .temperature(0.7)
 *     .maxTokens(100))
 * ```
 *
 * @param requestBuilder The builder instance configured with desired chat
 *    request parameters.
 * @return [ChatResponse] containing the model's response and any additional
 *    metadata.
 * @throws Exception if the chat request fails, is interrupted, or the builder
 *    produces an invalid configuration.
 * @see ChatRequest
 * @see ChatResponse
 * @see ChatRequest.Builder
 * @see chatAsync
 */
suspend fun ChatLanguageModel.chatAsync(requestBuilder: ChatRequest.Builder): ChatResponse =
    chatAsync(requestBuilder.build())

/**
 * Asynchronously processes a chat request by configuring a `ChatRequest`
 * using a provided builder block. This method facilitates the creation
 * of well-structured chat requests using a `ChatRequestBuilder` and
 * executes the request using the associated `ChatLanguageModel`.
 *
 * @param block A lambda with receiver on `ChatRequestBuilder` used to
 *    configure the messages and parameters for the chat request.
 * @return A `ChatResponse` containing the response from the model and any
 *    associated metadata.
 * @throws Exception if the chat request fails or encounters an error during execution.
 */
suspend fun ChatLanguageModel.chatAsync(block: ChatRequestBuilder.() -> Unit): ChatResponse =
    chatAsync(chatRequest(block))

/**
 * Processes a chat request using a [ChatRequest.Builder] for convenient request
 * configuration. This extension function provides a builder pattern alternative
 * to creating [ChatRequest] directly.
 *
 * Example usage:
 * ```kotlin
 * val response = model.chat(ChatRequest.builder()
 *     .messages(listOf(UserMessage("Hello")))
 *     .temperature(0.7)
 *     .maxTokens(100))
 * ```
 *
 * @param requestBuilder The builder instance configured with desired chat
 *    request parameters.
 * @return [ChatResponse] containing the model's response and any additional
 *    metadata.
 * @throws Exception if the chat request fails or the builder produces an
 *    invalid configuration.
 * @see ChatRequest
 * @see ChatResponse
 * @see ChatRequest.Builder
 */
fun ChatLanguageModel.chat(requestBuilder: ChatRequest.Builder): ChatResponse =
    this.chat(requestBuilder.build())

/**
 * Asynchronously generates a response for a list of chat messages using
 * the language model within a coroutine scope. This extension function
 * provides a structured concurrency wrapper around the synchronous
 * [ChatLanguageModel.generate] method.
 *
 * Example usage:
 * ```kotlin
 * val response = model.generateAsync(listOf(
 *     SystemMessage("You are a helpful assistant"),
 *     UserMessage("Hello!")
 * ))
 * ```
 *
 * @param messages The list of chat messages representing the conversation
 *    history and current prompt.
 * @return [Response] containing the AI's message and any additional metadata.
 * @throws Exception if the generation fails or is interrupted.
 * @see ChatLanguageModel.generate
 * @see ChatMessage
 * @see Response
 * @see AiMessage
 */
suspend fun ChatLanguageModel.generateAsync(messages: List<ChatMessage>): Response<AiMessage> {
    val model = this
    return coroutineScope { model.generate(messages) }
}

/**
 * Asynchronously generates a response from the chat language model based on the provided messages.
 *
 * @param messages A variable number of chat messages that serve as the input for the language model's generation.
 *                 This typically includes the conversation history and the current prompt.
 * @return A [Response] containing the generated [AiMessage] from the language model.
 */
suspend fun ChatLanguageModel.generateAsync(vararg messages: ChatMessage): Response<AiMessage> =
    this.generateAsync(messages.toList())
