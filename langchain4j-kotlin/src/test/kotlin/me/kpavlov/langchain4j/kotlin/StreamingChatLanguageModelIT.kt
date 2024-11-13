package me.kpavlov.langchain4j.kotlin

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.test.runTest
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatLanguageModelReply.Completion
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatLanguageModelReply.Token
import me.kpavlov.langchain4j.kotlin.model.chat.generateFlow
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

            val messages =
                listOf<ChatMessage>(
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
                )

            val responseRef = AtomicReference<Response<AiMessage>?>()

            val collectedTokens = mutableListOf<String>()

            model
                .generateFlow(messages)
                .collect {
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

            val response = responseRef.get()!!
            assertThat(response.metadata()).isNotNull()
            val content = response.content()
            assertThat(content).isNotNull()
            assertThat(collectedTokens.joinToString(""))
                .isEqualTo(content.text())
            assertThat(content.text()).contains("Blumblefang loves to help")
        }
}
