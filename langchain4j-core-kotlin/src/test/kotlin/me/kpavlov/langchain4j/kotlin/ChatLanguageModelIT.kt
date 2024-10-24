package me.kpavlov.langchain4j.kotlin

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isSuccess
import dev.langchain4j.data.document.DocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.source.FileSystemSource
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.generateAsync
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.nio.file.Paths

internal class ChatLanguageModelIT {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val model: ChatLanguageModel = OpenAiChatModel
        .builder()
        .apiKey("demo")
        .modelName("gpt-4o-mini")
        .temperature(0.0)
        .maxTokens(1024)
        .build()

    @Test
    fun `ChatLanguageModel should generateAsync`() = runTest {

        val document = loadDocument("notes/blumblefang.txt", logger)

        val result = model.generateAsync(
            listOf(
                SystemMessage.from(
                    """
                    You are helpful advisor answering questions only related to the given text"""
                        .trimIndent()
                ),
                UserMessage.from(
                    """
                    What does Blumblefang love? Text: ```${document.text()}```
                    """.trimIndent()
                ),
            )
        )

        logger.info("Result: {}", result);
        assertThat(result).isSuccess()
        val response = result.getOrThrow()
        val content = response.content()
        assertThat(content.text()).contains("Blumblefang loves to help")
    }
}