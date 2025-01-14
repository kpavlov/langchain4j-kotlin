package me.kpavlov.langchain4j.kotlin.model.chat.request

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequestParameters
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ToolChoice
import dev.langchain4j.model.openai.OpenAiChatRequestParameters
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

        assertThat(result.messages()).containsExactly(
            systemMessage,
            userMessage,
        )
        assertThat(result.parameters()).isEqualTo(params)
        assertThat(result.parameters().temperature()).isNotEqualTo(0.1)
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
        assertThat(parameters).isInstanceOf(DefaultChatRequestParameters::class)
        assertThat(parameters.temperature()).isCloseTo(0.1, 0.000001)
        assertThat(parameters.modelName()).isEqualTo("super-model")
        assertThat(parameters.topP()).isCloseTo(0.2, 0.000001)
        assertThat(parameters.topK()).isEqualTo(3)
        assertThat(parameters.frequencyPenalty()).isCloseTo(0.4, 0.000001)
        assertThat(parameters.presencePenalty()).isCloseTo(0.5, 0.000001)
        assertThat(parameters.maxOutputTokens()).isEqualTo(6)
        assertThat(parameters.stopSequences()).containsExactly("halt", "stop")
        assertThat(parameters.toolSpecifications()).containsExactly(toolSpec)
        assertThat(parameters.toolChoice()).isEqualTo(ToolChoice.REQUIRED)
        assertThat(parameters.responseFormat()).isEqualTo(ResponseFormat.JSON)
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
        assertThat(parameters.temperature()).isCloseTo(0.1, 0.000001)
        assertThat(parameters.seed()).isEqualTo(42)
    }
}
