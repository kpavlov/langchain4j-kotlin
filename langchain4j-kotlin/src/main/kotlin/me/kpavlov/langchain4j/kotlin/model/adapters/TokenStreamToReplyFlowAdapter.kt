package me.kpavlov.langchain4j.kotlin.model.adapters

import dev.langchain4j.service.TokenStream
import dev.langchain4j.spi.services.TokenStreamAdapter
import kotlinx.coroutines.flow.Flow
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply
import me.kpavlov.langchain4j.kotlin.service.asReplyFlow
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

public class TokenStreamToReplyFlowAdapter : TokenStreamAdapter {
    override fun canAdaptTokenStreamTo(type: Type?): Boolean {
        if (type is ParameterizedType && type.rawType === Flow::class.java) {
            val typeArguments: Array<Type> = type.actualTypeArguments
            return typeArguments.size == 1 &&
                typeArguments[0] === StreamingChatModelReply::class.java
        }
        return false
    }

    override fun adapt(tokenStream: TokenStream): Any = tokenStream.asReplyFlow()
}
