package me.kpavlov.langchain4j.kotlin.service.memory

import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.service.memory.ChatMemoryService
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
public class ChatMemoryServiceExtensionsTest {
    @Mock
    private lateinit var chatMemoryService: ChatMemoryService

    @Mock
    private lateinit var chatMemory: ChatMemory

    private val memoryId: ChatMemoryId = "test-memory-id"

    @Test
    public fun `getOrCreateChatMemoryAsync should call getOrCreateChatMemory on service`(): Unit = runTest {
        // Arrange
        whenever(chatMemoryService.getOrCreateChatMemory(memoryId)).thenReturn(chatMemory)

        // Act
        val result = chatMemoryService.getOrCreateChatMemoryAsync(memoryId)

        // Assert
        result shouldBe chatMemory
        verify(chatMemoryService).getOrCreateChatMemory(memoryId)
    }

    @Test
    public fun `getChatMemoryAsync should call getChatMemory on service`(): Unit = runTest {
        // Arrange
        whenever(chatMemoryService.getChatMemory(memoryId)).thenReturn(chatMemory)

        // Act
        val result = chatMemoryService.getChatMemoryAsync(memoryId)

        // Assert
        result shouldBe chatMemory
        verify(chatMemoryService).getChatMemory(memoryId)
    }

    @Test
    public fun `evictChatMemoryAsync should call evictChatMemory on service`(): Unit = runTest {
        // Arrange
        whenever(chatMemoryService.evictChatMemory(memoryId)).thenReturn(chatMemory)

        // Act
        val result = chatMemoryService.evictChatMemoryAsync(memoryId)

        // Assert
        result shouldBe chatMemory
        verify(chatMemoryService).evictChatMemory(memoryId)
    }

    @Test
    public fun `evictChatMemoryAsync should handle null response`(): Unit = runTest {
        // Arrange
        whenever(chatMemoryService.evictChatMemory(memoryId)).thenReturn(null)

        // Act
        val result = chatMemoryService.evictChatMemoryAsync(memoryId)

        // Assert
        result shouldBe null
        verify(chatMemoryService).evictChatMemory(memoryId)
    }
}
