package me.kpavlov.langchain4j.kotlin.model.chat.request

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ChatRequestParameters
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ToolChoice

/**
 * Builds and returns a `ChatRequest` using the provided configuration block.
 * The configuration is applied on a `ChatRequestBuilder` instance to customize
 * messages and parameters that will be part of the resulting `ChatRequest`.
 *
 * @param block A lambda with receiver on `ChatRequestBuilder` to configure messages
 * and/or parameters for the `ChatRequest`.
 * @return A fully constructed `ChatRequest` instance based on the applied configurations.
 */
public fun chatRequest(block: ChatRequestBuilder.() -> Unit): ChatRequest {
    val builder = ChatRequestBuilder()
    builder.apply { block() }
    return builder.build()
}

/**
 * A utility class for building and configuring chat request parameters. This builder allows fine-grained
 * control over various fields such as model configuration, response shaping, and tool integration.
 *
 * @param B The type of the builder for the default chat request parameters.
 * @property builder The builder used to configure the chat request parameters.
 * @property modelName Specifies the name of the model to be used for the chat request.
 * @property temperature Controls the randomness in the response generation. Higher values produce more random outputs.
 * @property topP Configures nucleus sampling, limiting the selection to a subset of tokens
 * with a cumulative probability of `topP`.
 * @property topK Limits the selection to the top `K` tokens during response generation.
 * @property frequencyPenalty Applies a penalty to discourage repetition of tokens based on frequency.
 * @property presencePenalty Applies a penalty to encourage diversity by penalizing token presence
 * in the conversation context.
 * @property maxOutputTokens Specifies the maximum number of tokens for the generated response.
 * @property stopSequences A list of sequences that will terminate the response generation if encountered.
 * @property toolSpecifications A list of tool specifications for integrating external tools into the chat request.
 * @property toolChoice Defines the specific tool to be used if multiple tools are available in the request.
 * @property responseFormat Specifies the format of the response, such as plain text or structured data.
 */
@Suppress("LongParameterList")
public open class ChatRequestParametersBuilder<B : DefaultChatRequestParameters.Builder<*>>(
    public val builder: B,
    public var modelName: String? = null,
    public var temperature: Double? = null,
    public var topP: Double? = null,
    public var topK: Int? = null,
    public var frequencyPenalty: Double? = null,
    public var presencePenalty: Double? = null,
    public var maxOutputTokens: Int? = null,
    public var stopSequences: List<String>? = null,
    public var toolSpecifications: List<ToolSpecification>? = null,
    public var toolChoice: ToolChoice? = null,
    public var responseFormat: ResponseFormat? = null,
)

/**
 * Builder class for constructing a `ChatRequest` instance. Allows configuring
 * messages and request parameters to customize the resulting request.
 *
 * This builder provides methods to add individual or multiple chat messages,
 * as well as set request parameters for the generated `ChatRequest`.
 */
public open class ChatRequestBuilder(
    public var messages: MutableList<ChatMessage> = mutableListOf(),
    public var parameters: ChatRequestParameters? = null,
) {
    /**
     * Adds a list of `ChatMessage` objects to the builder's messages collection.
     *
     * @param value The list of `ChatMessage` objects to be added to the builder.
     * @return This builder instance for chaining other method calls.
     */
    public fun messages(value: List<ChatMessage>): ChatRequestBuilder =
        apply { this.messages.addAll(value) }

    /**
     * Adds a chat message to the messages list.
     *
     * @param value The chat message to be added.
     * @return The current instance for method chaining.
     */
    public fun message(value: ChatMessage): ChatRequestBuilder = apply { this.messages.add(value) }

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
     * Defaults to an instance of [DefaultChatRequestParameters.Builder].
     * @param configurer A lambda with the builder as receiver to configure the chat request parameters.
     */
    @JvmOverloads
    public fun <B : DefaultChatRequestParameters.Builder<*>> parameters(
        @Suppress("UNCHECKED_CAST")
        builder: B = DefaultChatRequestParameters.builder() as B,
        configurer: ChatRequestParametersBuilder<B>.() -> Unit,
    ) {
        val b = ChatRequestParametersBuilder(builder = builder).also(configurer)
        parameters =
            builder
                .apply {
                    b.modelName?.let { modelName(it) }
                    b.temperature?.let { temperature(it) }
                    b.topP?.let { topP(it) }
                    b.topK?.let { topK(it) }
                    b.frequencyPenalty?.let { frequencyPenalty(it) }
                    b.presencePenalty?.let { presencePenalty(it) }
                    b.maxOutputTokens?.let { maxOutputTokens(it) }
                    b.stopSequences?.let { stopSequences(it) }
                    b.toolSpecifications?.let { toolSpecifications(it) }
                    b.toolChoice?.let { toolChoice(it) }
                    b.responseFormat?.let { responseFormat(it) }
                }.build()
    }
}
