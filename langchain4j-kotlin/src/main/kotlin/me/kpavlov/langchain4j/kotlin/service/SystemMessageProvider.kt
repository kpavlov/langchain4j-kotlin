package me.kpavlov.langchain4j.kotlin.service

import me.kpavlov.langchain4j.kotlin.ChatMemoryId

/**
 * Interface for providing LLM system messages based on a given chat memory identifier.
 */
public interface SystemMessageProvider {
    /**
     * Provides a system message based on the given chat memory identifier.
     *
     * @param chatMemoryID Identifier for the chat memory used to generate the system message.
     * @return A system prompt string associated with the provided chat memory identifier, maybe `null`
     */
    public fun getSystemMessage(chatMemoryID: ChatMemoryId): String?
}
