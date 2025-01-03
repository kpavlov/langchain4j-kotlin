package me.kpavlov.langchain4j.kotlin

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.test.runTest
import me.kpavlov.langchain4j.kotlin.model.chat.chatAsync
import me.kpavlov.langchain4j.kotlin.model.chat.generateAsync
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(
    named = "OPENAI_API_KEY",
    matches = ".+",
)
internal class ChatLanguageModelIT {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val model: ChatLanguageModel =
        OpenAiChatModel
            .builder()
            .apiKey(TestEnvironment.openaiApiKey)
            .modelName("gpt-4o-mini")
            .temperature(0.0)
            .maxTokens(1024)
            .build()

    private lateinit var document: Document

    @BeforeAll
    fun beforeAll() =
        runTest {
            document = loadDocument("notes/blumblefang.txt", logger)
        }

    @Test
    fun `ChatLanguageModel should generateAsync`() =
        runTest {
            val response =
                model.generateAsync(
                    listOf(
                        SystemMessage.from(
                            """
                            You are helpful advisor answering questions only related to the given text

                            """.trimIndent(),
                        ),
                        UserMessage.from(
                            """
                            What does Blumblefang love? Text: ```${document.text()}```
                            """.trimIndent(),
                        ),
                    ),
                )

            logger.info("Response: {}", response)
            assertThat(response).isNotNull()
            val content = response.content()
            assertThat(content.text()).contains("Blumblefang loves to help")
        }

    @Test
    fun `ChatLanguageModel should chatAsync`() =
        runTest {
            val document = loadDocument("notes/blumblefang.txt", logger)

            val response =
                model.chatAsync(
                    ChatRequest
                        .builder()
                        .messages(
                            listOf(
                                SystemMessage.from(
                                    """
                                    You are helpful advisor answering questions only related to the given text

                                    """.trimIndent(),
                                ),
                                UserMessage.from(
                                    """
                                    What does Blumblefang love? Text: ```${document.text()}```
                                    """.trimIndent(),
                                ),
                            ),
                        ).responseFormat(ResponseFormat.TEXT),
                )

            logger.info("Response: {}", response)
            assertThat(response).isNotNull()
            val content = response.aiMessage()
            assertThat(content.text()).contains("Blumblefang loves to help")
        }
}
