package me.kpavlov.langchain4j.kotlin.samples

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.mock.ChatModelMock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class OpenAiChatModelTest {

    private val model: ChatModel = ChatModelMock("Hello from Mock Model")

    @Test
    fun `OpenAiChatModel example should work`() = runTest {
        OpenAiChatModelExample(model).callChatAsync() shouldBe "Hello from Mock Model"
    }

    @Test
    fun `Async AiServices example should work`() = runTest {
        AsyncAiServiceExample(model).callAiService() shouldBe "Hello from Mock Model"
    }
}
