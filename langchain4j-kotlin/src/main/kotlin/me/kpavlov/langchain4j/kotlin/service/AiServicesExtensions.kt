package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.service.AiServiceContext
import dev.langchain4j.service.AiServices
import dev.langchain4j.spi.services.AiServicesFactory

/**
 * Creates an [AiServices] instance using the provided [AiServicesFactory].
 */
public fun <T> createAiService(
    serviceClass: Class<T>,
    factory: AiServicesFactory,
): AiServices<T> = AiServiceContext(serviceClass).let { context -> factory.create<T>(context) }
