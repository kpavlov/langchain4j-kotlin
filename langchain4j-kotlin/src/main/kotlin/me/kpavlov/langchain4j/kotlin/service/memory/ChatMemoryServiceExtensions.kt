package me.kpavlov.langchain4j.kotlin.service.memory

import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.service.memory.ChatMemoryService
import kotlinx.coroutines.coroutineScope
import me.kpavlov.langchain4j.kotlin.ChatMemoryId

public suspend fun ChatMemoryService.getOrCreateChatMemoryAsync(memoryId: ChatMemoryId): ChatMemory =
    coroutineScope { this@getOrCreateChatMemoryAsync.getOrCreateChatMemory(memoryId) }

public suspend fun ChatMemoryService.getChatMemoryAsync(memoryId: ChatMemoryId): ChatMemory =
    coroutineScope { this@getChatMemoryAsync.getChatMemory(memoryId) }

public suspend fun ChatMemoryService.evictChatMemoryAsync(memoryId: ChatMemoryId): ChatMemory =
    coroutineScope { this@evictChatMemoryAsync.evictChatMemory(memoryId) }
