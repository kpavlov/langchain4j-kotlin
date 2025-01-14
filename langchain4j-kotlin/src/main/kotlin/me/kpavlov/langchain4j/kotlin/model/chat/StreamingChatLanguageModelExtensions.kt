package me.kpavlov.langchain4j.kotlin.model.chat

import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import me.kpavlov.langchain4j.kotlin.model.chat.request.ChatRequestBuilder
import me.kpavlov.langchain4j.kotlin.model.chat.request.chatRequest
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(StreamingChatLanguageModel::class.java)

/**
 * Represents different types of replies that can be received from an AI language model during streaming.
 * This sealed interface provides type-safe handling of both intermediate tokens and final completion responses.
 */
sealed interface StreamingChatLanguageModelReply {
    /**
     * Represents a partial response received from an AI language model during a streaming interaction.
     *
     * This data class is used to encapsulate an intermediate token that the model generates as part of its
     * streaming output. Partial responses are often used in scenarios where the model's output is produced
     * incrementally, enabling real-time updates to the user or downstream processes.
     *
     * @property token The string representation of the token generated as part of the streaming process.
     * @see StreamingChatResponseHandler.onPartialResponse
     */
    data class PartialResponse(
        val token: String,
    ) : StreamingChatLanguageModelReply

    /**
     * Represents a final completion response received from the AI language model
     * during the streaming chat process.
     *
     * This data class encapsulates the complete response, which typically contains
     * the final output of a model's reply in the context of a conversation.
     *
     * @property response The final chat response generated by the model.
     * @see StreamingChatResponseHandler.onCompleteResponse
     */
    data class CompleteResponse(
        val response: ChatResponse,
    ) : StreamingChatLanguageModelReply

    /**
     * Represents an error that occurred during the streaming process
     * when generating a reply from the AI language model. This type
     * of reply is used to indicate a failure in the operation and
     * provides details about the cause of the error.
     *
     * @property cause The underlying exception or error that caused the failure.
     * @see StreamingChatResponseHandler.onError
     */
    data class Error(
        val cause: Throwable,
    ) : StreamingChatLanguageModelReply
}

/**
 * Converts a streaming chat language model into a Kotlin [Flow] of [StreamingChatLanguageModelReply]
 * events. This extension function provides a coroutine-friendly way to consume streaming responses
 * from the language model.
 *
 * The method uses a provided configuration block to build a chat request
 * and manages the streaming process by handling partial responses, complete
 * responses, and errors through a LC4J's [dev.langchain4j.model.chat.response.StreamingChatResponseHandler].
 *
 * @param block A lambda with receiver on [ChatRequestBuilder] used to configure
 * the [dev.langchain4j.model.chat.request.ChatRequest] by adding messages and/or setting parameters.
 *
 * @return A [Flow] of [StreamingChatLanguageModelReply], which emits different
 * types of replies during the chat interaction, including partial responses,
 * final responses, and errors.
 */
fun StreamingChatLanguageModel.chatFlow(
    block: ChatRequestBuilder.() -> Unit,
): Flow<StreamingChatLanguageModelReply> =
    callbackFlow {
        val model = this@chatFlow
        val chatRequest = chatRequest(block)
        val handler =
            object : StreamingChatResponseHandler {
                override fun onPartialResponse(token: String) {
                    logger.trace(
                        me.kpavlov.langchain4j.kotlin.internal.SENSITIVE,
                        "Received partialResponse: {}",
                        token,
                    )
                    trySend(StreamingChatLanguageModelReply.PartialResponse(token))
                }

                override fun onCompleteResponse(completeResponse: ChatResponse) {
                    logger.trace(
                        me.kpavlov.langchain4j.kotlin.internal.SENSITIVE,
                        "Received completeResponse: {}",
                        completeResponse,
                    )
                    trySend(StreamingChatLanguageModelReply.CompleteResponse(completeResponse))
                    close()
                }

                override fun onError(error: Throwable) {
                    logger.error(
                        "Received error: {}",
                        error.message,
                        error,
                    )
                    trySend(StreamingChatLanguageModelReply.Error(error))
                    close(error)
                }
            }

        logger.info("Starting flow...")
        model.chat(chatRequest, handler)

        // This will be called when the flow collection is closed or cancelled.
        awaitClose {
            // cleanup
            logger.info("Flow is canceled")
        }
    }
