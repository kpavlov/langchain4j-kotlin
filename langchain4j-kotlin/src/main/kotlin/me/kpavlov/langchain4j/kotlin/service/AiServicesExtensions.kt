package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.service.AiServices

/**
 * Sets the system message provider for the AI services.
 *
 * @param provider The SystemMessageProvider that supplies system messages based on chat memory identifiers.
 * @return The updated AiServices instance with the specified system message provider.
 */
public fun <T> AiServices<T>.systemMessageProvider(
    provider: SystemMessageProvider,
): AiServices<T> = this.systemMessageProvider(provider::getSystemMessage)
