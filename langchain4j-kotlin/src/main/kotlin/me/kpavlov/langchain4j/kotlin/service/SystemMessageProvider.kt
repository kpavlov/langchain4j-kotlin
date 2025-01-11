package me.kpavlov.langchain4j.kotlin.service

import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import me.kpavlov.langchain4j.kotlin.PromptContent
import java.util.function.Function

/**
 * Interface for providing LLM system messages based on a given chat memory identifier.
 */
@FunctionalInterface
interface SystemMessageProvider : Function<ChatMemoryId, PromptContent?> {
    /**
     * Provides a system message based on the given chat memory identifier.
     *
     * @param chatMemoryID Identifier for the chat memory used to generate the system message.
     * @return A system prompt string associated with the provided chat memory identifier, maybe `null`
     */
    fun getSystemMessage(chatMemoryID: ChatMemoryId): PromptContent?

    /**
     * Applies the given chat memory identifier to generate the corresponding system message.
     *
     * @param chatMemoryID The identifier representing the chat memory to be used.
     * @return The prompt content associated with the specified chat memory identifier,
     * or `null` if no system message is available.
     */
    override fun apply(chatMemoryID: ChatMemoryId): PromptContent? = getSystemMessage(chatMemoryID)
}
