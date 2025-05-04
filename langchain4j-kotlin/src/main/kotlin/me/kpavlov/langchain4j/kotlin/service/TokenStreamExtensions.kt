package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.service.TokenStream
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply

public fun TokenStream.asFlow(): Flow<String> =
    flow {
        callbackFlow {
            onPartialResponse { trySend(it) }
            onCompleteResponse { close() }
            onError { close(it) }
            start()
            awaitClose()
        }.buffer(Channel.UNLIMITED).collect(this)
    }

public fun TokenStream.asReplyFlow(): Flow<StreamingChatModelReply> =
    flow {
        callbackFlow {
            onPartialResponse { token ->
                trySend(StreamingChatModelReply.PartialResponse(token))
            }
            onCompleteResponse { response ->
                trySend(StreamingChatModelReply.CompleteResponse(response))
                close()
            }
            onError { throwable -> close(throwable) }
            start()
            awaitClose()
        }.buffer(Channel.UNLIMITED).collect(this)
    }
