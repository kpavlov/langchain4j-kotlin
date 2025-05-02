package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.service.AiServiceContext
import dev.langchain4j.service.AiServices
import dev.langchain4j.spi.services.AiServicesFactory

public class AsyncAiServicesFactory : AiServicesFactory {
    override fun <T : Any> create(context: AiServiceContext): AiServices<T> =
        AsyncAiServices(context)
}
