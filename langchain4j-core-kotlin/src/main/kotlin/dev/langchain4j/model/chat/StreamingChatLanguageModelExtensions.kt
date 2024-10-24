package dev.langchain4j.model.chat

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.internal.PII
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(StreamingChatLanguageModel::class.java)

sealed interface AiReply {
    data class Token(val token: String) : AiReply
    data class Completion(val response: Response<AiMessage>) : AiReply
}

fun StreamingChatLanguageModel.generateFlow(
    messages: List<ChatMessage>,
): Flow<AiReply> = callbackFlow {

    val model = this@generateFlow

    val handler = object : StreamingResponseHandler<AiMessage> {
        override fun onNext(token: String) {
            logger.trace(PII, "Received token: {}", token)
            trySend(AiReply.Token(token))
        }

        override fun onComplete(response: Response<AiMessage>) {
            logger.trace(PII, "Received response: {}", response)
            trySend(AiReply.Completion(response))
            close()
        }

        override fun onError(error: Throwable) {
            close(error)
        }
    }

    logger.info("Starting flow...")
    model.generate(messages, handler)

    // This will be called when the flow collection is closed or cancelled.
    awaitClose {
        // cleanup
        logger.info("Flow is canceled")
    }
}




