package me.kpavlov.langchain4j.kotlin.service.memory

import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.service.memory.ChatMemoryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import kotlin.coroutines.CoroutineContext

public suspend fun ChatMemoryService.getOrCreateChatMemoryAsync(
    memoryId: ChatMemoryId,
    context: CoroutineContext = Dispatchers.IO,
): ChatMemory =
    withContext(context) { this@getOrCreateChatMemoryAsync.getOrCreateChatMemory(memoryId) }

public suspend fun ChatMemoryService.getChatMemoryAsync(
    memoryId: ChatMemoryId,
    context: CoroutineContext = Dispatchers.IO,
): ChatMemory? =
    withContext(context)
    { this@getChatMemoryAsync.getChatMemory(memoryId) }

public suspend fun ChatMemoryService.evictChatMemoryAsync(
    memoryId: ChatMemoryId,
    context: CoroutineContext = Dispatchers.IO,
): ChatMemory? =
    withContext(context) { this@evictChatMemoryAsync.evictChatMemory(memoryId) }
