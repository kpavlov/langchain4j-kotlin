package me.kpavlov.langchain4j.kotlin.model.chat

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import kotlinx.coroutines.test.runTest
import me.kpavlov.langchain4j.kotlin.TestEnvironment
import me.kpavlov.langchain4j.kotlin.loadDocument
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatLanguageModelReply.CompleteResponse
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatLanguageModelReply.PartialResponse
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

@EnabledIfEnvironmentVariable(
    named = "OPENAI_API_KEY",
    matches = ".+",
)
class StreamingChatLanguageModelIT {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val model: StreamingChatLanguageModel =
        OpenAiStreamingChatModel
            .builder()
            .apiKey(TestEnvironment.openaiApiKey)
            .modelName("gpt-4o-mini")
            .temperature(0.0)
            .maxTokens(100)
            .build()

    @Test
    fun `StreamingChatLanguageModel should generateFlow`() =
        runTest {
            val document = loadDocument("notes/blumblefang.txt", logger)

            val systemMessage =
                systemMessage(
                    """
                    You are helpful advisor answering questions only related to the given text
                    """.trimIndent(),
                )
            val userMessage =
                userMessage(
                    """
                    What does Blumblefang love? Text: ```${document.text()}```
                    """.trimIndent(),
                )

            val responseRef = AtomicReference<ChatResponse?>()

            val collectedTokens = mutableListOf<String>()

            model
                .chatFlow {
                    messages += systemMessage
                    messages += userMessage
                }.collect {
                    when (it) {
                        is PartialResponse -> {
                            println("Token: '${it.token}'")
                            collectedTokens.add(it.token)
                        }

                        is CompleteResponse -> responseRef.set(it.response)
                        else -> fail("Unsupported event: $it")
                    }
                }

            val response = responseRef.get()!!
            assertThat(response.metadata()).isNotNull()
            assertThat(response.aiMessage()).isNotNull()
            val textContent = response.aiMessage()?.text()!!
            assertThat(textContent).isNotNull()
            assertThat(collectedTokens.joinToString("")).isEqualTo(textContent)
            assertThat(textContent).contains("Blumblefang loves to help")
        }
}
