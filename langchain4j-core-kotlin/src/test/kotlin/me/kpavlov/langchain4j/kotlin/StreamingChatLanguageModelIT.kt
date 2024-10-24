package me.kpavlov.langchain4j.kotlin

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import dev.langchain4j.data.document.DocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.source.FileSystemSource
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Disabled("Run it manually")
class StreamingChatLanguageModelIT {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val model: StreamingChatLanguageModel = OpenAiStreamingChatModel
        .builder()
        .apiKey(TestEnvironment.dotenv.get("OPENAI_API_KEY"))
        .modelName("gpt-4o-mini")
        .temperature(0.0)
        .maxTokens(100)
        .build()

    @Test
    fun `Run test`() = runTest {

        val source = FileSystemSource(Paths.get("./src/test/resources/data/notes/blumblefang.txt"))
        val document = DocumentLoader.load(source, TextDocumentParser())

        with(document) {
            logger.info("Document Metadata: {}", metadata())
            logger.info("Document Text: {}", text())
        }

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

        model.generate(messages, object : StreamingResponseHandler<AiMessage> {
            override fun onNext(token: String) {
                logger.info("Token: {}", token);
            }

            override fun onComplete(response: Response<AiMessage>?) {
                logger.info("Response: {}", response);
                responseRef.set(response)
            }

            override fun onError(error: Throwable) {
                logger.error("Error: {}", error.message, error);
                fail(error)
            }
        })

        await
            .timeout(15.seconds.toJavaDuration())
            .until {
                responseRef.get() != null
            }

        val response = responseRef.get()!!
        assertThat(response.metadata()).isNotNull()
        val content = response.content()
        assertThat(content).isNotNull()
        assertThat(content.text()).contains("Blumblefang loves to help")
    }
}