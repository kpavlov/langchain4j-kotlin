package me.kpavlov.langchain4j.kotlin

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.StreamingChatLanguageModelReply
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModelReply.Completion
import dev.langchain4j.model.chat.StreamingChatLanguageModelReply.Token
import dev.langchain4j.model.chat.generateFlow
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Disabled("Run it manually")
class StreamingChatLanguageModelIT {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val model: StreamingChatLanguageModel = OpenAiStreamingChatModel
        .builder()
        .apiKey(TestEnvironment.env("OPENAI_API_KEY"))
        .modelName("gpt-4o-mini")
        .temperature(0.0)
        .maxTokens(100)
        .build()

    @Test
    fun `StreamingChatLanguageModel should generateFlow`() = runTest {
        val document = loadDocument("notes/blumblefang.txt", logger)

        val messages = listOf<ChatMessage>(
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

        val responseRef = AtomicReference<Response<AiMessage>?>()

        val flow = model.generateFlow(messages)

        val collectedTokens = mutableListOf<String>()

        flow.collect {
            logger.info("Received event: $it")
            when (it) {
                is Token -> {
                    logger.info("Token: '${it.token}'")
                    collectedTokens.add(it.token)
                }

                is Completion -> responseRef.set(it.response)
                else -> fail("Unsupported event: $it")
            }
        }

        await
            .timeout(15.seconds.toJavaDuration())
            .until {
                responseRef.get() != null
            }

        val response = responseRef.get()!!
        assertThat(response.metadata()).isNotNull()
        val content = response.content()
        assertThat(content).isNotNull()
        assertThat(collectedTokens.joinToString(""))
            .isEqualTo(content.text())
        assertThat(content.text()).contains("Blumblefang loves to help")
    }
}