package me.kpavlov.langchain4j.kotlin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.output.Response
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.UserName
import dev.langchain4j.service.V
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ServiceWithPromptTemplatesTest {
    private lateinit var model: ChatLanguageModel

    @Test
    fun `Should use System and User Prompt Templates`() {
        model =
            ChatLanguageModel {
                assertThat(
                    it[0],
                ).isEqualTo(
                    SystemMessage.from("You are helpful assistant using chatMemoryID=default"),
                )
                assertThat(it[1])
                    .isInstanceOf(UserMessage::class.java)
                    .given { userPrompt ->
                        assertThat(
                            userPrompt.singleText(),
                        ).isEqualTo("Hello, My friend! How are you?")
                    }
                Response.from(AiMessage.from("I'm fine, thanks"))
            }

        val assistant =
            AiServices
                .builder(Assistant::class.java)
                .systemMessageProvider(
                    TemplateSystemMessageProvider(
                        "prompts/ServiceWithTemplatesTest/default-system-prompt.mustache",
                    ),
                ).chatLanguageModel(model)
                .build()

        val response = assistant.askQuestion(userName = "My friend", question = "How are you?")
        assertThat(response).isEqualTo("I'm fine, thanks")
    }

    @Suppress("unused")
    private interface Assistant {
        @dev.langchain4j.service.UserMessage(
            "prompts/ServiceWithTemplatesTest/default-user-prompt.mustache",
        )
        fun askQuestion(
            @UserName userName: String,
            @V("message") question: String,
        ): String
    }
}
