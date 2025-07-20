package me.kpavlov.langchain4j.kotlin.model.chat

import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import me.kpavlov.langchain4j.kotlin.TestEnvironment
import me.kpavlov.langchain4j.kotlin.TestEnvironment.mockOpenAi
import me.kpavlov.langchain4j.kotlin.loadDocument
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply.CompleteResponse
import me.kpavlov.langchain4j.kotlin.model.chat.StreamingChatModelReply.PartialResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

internal open class StreamingChatModelIT {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val model: StreamingChatModel = createOpenAiStreamingModel()

    @AfterEach
    fun afterEach() {
        mockOpenAi.verifyNoUnmatchedRequests()
    }

    @Test
    fun `StreamingChatModel should generateFlow`() =
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

            setupMockResponseIfNecessary(
                systemMessage.text(),
                "What does Blumblefang love",
                "Blumblefang loves to help and cookies",
            )

            val responseRef = AtomicReference<ChatResponse?>()

            val collectedTokens = ConcurrentLinkedQueue<String>()

            model
                .chatFlow {
                    messages += systemMessage
                    messages += userMessage
                }
                .collect {
                    when (it) {
                        is PartialResponse -> {
                            println("Token: '${it.token}'")
                            collectedTokens += it.token
                        }

                        is CompleteResponse -> responseRef.set(it.response)
                        is StreamingChatModelReply.Error -> fail("Error", it.cause)
                    }
                }

            val response = responseRef.get()!!
            response.metadata().shouldNotBeNull()
            response.aiMessage().shouldNotBeNull {
                text() shouldContain "Blumblefang loves to help"
            }
        }

    fun setupMockResponseIfNecessary(
        expectedSystemMessage: String,
        expectedUserMessage: String,
        expectedAnswer: String,
    ) {
        if (TestEnvironment["OPENAI_API_KEY"] != null) {
            logger.error("Running with real OpenAI API")
            return
        }
        logger.error("Running with Mock OpenAI API (Ai-Mocks/Mokksy)")

        mockOpenAi.completion {
            requestBodyContains(expectedSystemMessage)
            requestBodyContains(expectedUserMessage)
        } respondsStream {
            responseFlow =
                flow {
                    expectedAnswer.split(" ").forEach { token ->
                        emit("$token ")
                        yield()
                        delay(42)
                    }
                }
            sendDone = true
        }
    }
}
