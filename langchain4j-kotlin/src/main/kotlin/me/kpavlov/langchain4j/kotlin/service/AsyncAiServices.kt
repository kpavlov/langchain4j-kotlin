package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.service.AiServiceContext
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.ChatMemoryAccess
import dev.langchain4j.service.IllegalConfigurationException.illegalConfiguration
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.Moderate
import dev.langchain4j.service.Result
import dev.langchain4j.service.TypeUtils
import dev.langchain4j.service.output.ServiceOutputParser
import dev.langchain4j.spi.ServiceHelper
import dev.langchain4j.spi.services.TokenStreamAdapter
import java.lang.reflect.Proxy

public class AsyncAiServices<T : Any>(
    context: AiServiceContext,
) : AiServices<T>(context) {
    private val serviceOutputParser = ServiceOutputParser()
    private val tokenStreamAdapters =
        ServiceHelper.loadFactories<TokenStreamAdapter>(TokenStreamAdapter::class.java)

    @Suppress("NestedBlockDepth")
    override fun build(): T {
        performBasicValidation()

        if (!context.hasChatMemory() &&
            ChatMemoryAccess::class.java.isAssignableFrom(context.aiServiceClass)
        ) {
            throw illegalConfiguration(
                "In order to have a service implementing ChatMemoryAccess, " +
                    "please configure the ChatMemoryProvider on the '%s'.",
                context.aiServiceClass.name,
            )
        }

        for (method in context.aiServiceClass.methods) {
            if (method.isAnnotationPresent(Moderate::class.java) &&
                context.moderationModel == null
            ) {
                throw illegalConfiguration(
                    "The @Moderate annotation is present, but the moderationModel is not set up. " +
                        "Please ensure a valid moderationModel is configured " +
                        "before using the @Moderate annotation.",
                )
            }
            if (method.returnType in
                arrayOf(
                    // supported collection types
                    Result::class.java,
                    MutableList::class.java,
                    MutableSet::class.java,
                )
            ) {
                TypeUtils.validateReturnTypesAreProperlyParametrized(
                    method.name,
                    method.genericReturnType,
                )
            }

            if (!context.hasChatMemory()) {
                for (parameter in method.parameters) {
                    if (parameter.isAnnotationPresent(MemoryId::class.java)) {
                        throw illegalConfiguration(
                            "In order to use @MemoryId, please configure " +
                                "ChatMemoryProvider on the '%s'.",
                            context.aiServiceClass.name,
                        )
                    }
                }
            }
        }

        val proxyInstance =
            Proxy.newProxyInstance(
                context.aiServiceClass.classLoader,
                arrayOf(context.aiServiceClass),
                ServiceInvocationHandler<T>(context, serviceOutputParser, tokenStreamAdapters),
            )

        @Suppress("UNCHECKED_CAST")
        return proxyInstance as T
    }
}
