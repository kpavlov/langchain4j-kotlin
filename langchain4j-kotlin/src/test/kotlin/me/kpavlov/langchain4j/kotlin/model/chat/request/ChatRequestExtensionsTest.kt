package me.kpavlov.langchain4j.kotlin.model.chat.request

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequestParameters
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ToolChoice
import dev.langchain4j.model.openai.OpenAiChatRequestParameters
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class ChatRequestExtensionsTest {
    @Test
    fun `Should build ChatRequest`() {
        val systemMessage = SystemMessage("You are a helpful assistant")
        val userMessage = UserMessage("Send greeting")
        val params: ChatRequestParameters = mock()
        val result =
            chatRequest {
                messages += systemMessage
                messages += userMessage
                parameters {
                    temperature = 0.1
                }
                parameters = params
            }

        result.messages() shouldContainExactly listOf(
            systemMessage,
            userMessage,
        )
        result.parameters() shouldBe params
        result.parameters().temperature() shouldNotBe 0.1
    }

    @Test
    fun `Should build ChatRequest with parameters builder`() {
        val systemMessage = SystemMessage("You are a helpful assistant")
        val userMessage = UserMessage("Send greeting")
        val toolSpec: ToolSpecification = mock()
        val toolSpecs = listOf(toolSpec)
        val result =
            chatRequest {
                messages += systemMessage
                messages += userMessage
                parameters {
                    temperature = 0.1
                    modelName = "super-model"
                    topP = 0.2
                    topK = 3
                    frequencyPenalty = 0.4
                    presencePenalty = 0.5
                    maxOutputTokens = 6
                    stopSequences = listOf("halt", "stop")
                    toolSpecifications = toolSpecs
                    toolChoice = ToolChoice.REQUIRED
                    responseFormat = ResponseFormat.JSON
                }
            }
        val parameters = result.parameters()
        parameters should beInstanceOf<DefaultChatRequestParameters>()
        parameters.temperature().shouldBeWithinPercentageOf(0.1, 0.000001)
        parameters.modelName() shouldBe "super-model"
        parameters.topP().shouldBeWithinPercentageOf(0.2, 0.000001)
        parameters.topK() shouldBe 3
        parameters.frequencyPenalty().shouldBeWithinPercentageOf(0.4, 0.000001)
        parameters.presencePenalty().shouldBeWithinPercentageOf(0.5, 0.000001)
        parameters.maxOutputTokens() shouldBe 6
        parameters.stopSequences() shouldContainExactly listOf("halt", "stop")
        parameters.toolSpecifications() shouldContainExactly listOf(toolSpec)
        parameters.toolChoice() shouldBe ToolChoice.REQUIRED
        parameters.responseFormat() shouldBe ResponseFormat.JSON
    }

    @Test
    fun `Should build ChatRequest with OpenAi parameters builder`() {
        val systemMessage = SystemMessage("You are a helpful assistant")
        val userMessage = UserMessage("Send greeting")
        val result =
            chatRequest {
                messages += systemMessage
                messages += userMessage
                parameters(OpenAiChatRequestParameters.builder()) {
                    temperature = 0.1
                    builder.seed(42)
                }
            }
        val parameters = result.parameters() as OpenAiChatRequestParameters
        parameters.temperature().shouldBeWithinPercentageOf(0.1, 0.000001)
        parameters.seed() shouldBe 42
    }
}
