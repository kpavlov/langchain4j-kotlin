package me.kpavlov.langchain4j.kotlin.model.chat.request

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ChatRequestParameters
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters

/**
 * Builds and returns a `ChatRequest` using the provided configuration block.
 * The configuration is applied on a `ChatRequestBuilder` instance to customize
 * messages and parameters that will be part of the resulting `ChatRequest`.
 *
 * @param block A lambda with receiver on `ChatRequestBuilder` to configure messages
 * and/or parameters for the `ChatRequest`.
 * @return A fully constructed `ChatRequest` instance based on the applied configurations.
 */
fun chatRequest(block: ChatRequestBuilder.() -> Unit): ChatRequest {
    val builder = ChatRequestBuilder()
    builder.apply { block() }
    return builder.build()
}

/**
 * Builder class for constructing a `ChatRequest` instance. Allows configuring
 * messages and request parameters to customize the resulting request.
 *
 * This builder provides methods to add individual or multiple chat messages,
 * as well as set request parameters for the generated `ChatRequest`.
 */
open class ChatRequestBuilder(
    var messages: MutableList<ChatMessage> = mutableListOf(),
    var parameters: ChatRequestParameters? = null,
) {
    /**
     * Adds a list of `ChatMessage` objects to the builder's messages collection.
     *
     * @param value The list of `ChatMessage` objects to be added to the builder.
     * @return This builder instance for chaining other method calls.
     */
    fun messages(value: List<ChatMessage>) = apply { this.messages.addAll(value) }

    /**
     * Adds a chat message to the messages list.
     *
     * @param value The chat message to be added.
     * @return The current instance for method chaining.
     */
    fun message(value: ChatMessage) = apply { this.messages.add(value) }

    /**
     * Builds and returns a ChatRequest instance using the current state of messages and parameters.
     *
     * @return A new instance of ChatRequest configured with the provided messages and parameters.
     */
    internal fun build(): ChatRequest =
        ChatRequest
            .Builder()
            .messages(this.messages)
            .parameters(this.parameters)
            .build()

    /**
     * Configures and sets the parameters for the chat request.
     *
     * @param builder The builder instance used to create the chat request parameters.
     * Defaults to an instance of `DefaultChatRequestParameters.Builder`.
     * @param block A lambda with the builder as receiver to configure the chat request parameters.
     */
    @JvmOverloads
    fun <B : DefaultChatRequestParameters.Builder<*>> parameters(
        @Suppress("UNCHECKED_CAST")
        builder: B = DefaultChatRequestParameters.builder() as B,
        block: B.() -> Unit,
    ) {
        this.parameters = builder.apply(block).build()
    }
}
