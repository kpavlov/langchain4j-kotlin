package me.kpavlov.langchain4j.kotlin.model.chat

import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
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
suspend fun ChatLanguageModel.chatAsync(request: ChatRequest): ChatResponse =
    coroutineScope { this@chatAsync.chat(request) }

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
 * Example usage:
 * ```kotlin
 * model.chatAsync {
 *     messages += systemMessage("You are a helpful assistant")
 *     messages += userMessage("Say Hello")
 *     parameters {
 *         temperature = 0.1
 *     }
 * }
 * ```
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
 * val response = model.chat(chatRequest{
 *     messages += userMessage("Hello")
 *     parameters {
 *         temperature = 0.7
 *         maxOutputTokens = 100
 *     }
 * })
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
 * @see ChatRequestBuilder
 */
fun ChatLanguageModel.chat(requestBuilder: ChatRequest.Builder): ChatResponse =
    this.chat(requestBuilder.build())
