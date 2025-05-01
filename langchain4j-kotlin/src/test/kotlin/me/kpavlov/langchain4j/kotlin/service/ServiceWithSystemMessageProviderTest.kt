package me.kpavlov.langchain4j.kotlin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.output.Response
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.UserMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ServiceWithSystemMessageProviderTest {
    @Mock
    lateinit var model: ChatLanguageModel

    @Test
    fun `Should use SystemMessageProvider`() {
        model =
            ChatLanguageModel {
                assertThat(it.first()).isEqualTo(SystemMessage.from("You are helpful assistant"))
                Response.from(AiMessage.from("I'm fine, thanks"))
            }

        val assistant =
            AiServices
                .builder(Assistant::class.java)
                .systemMessageProvider(
                    object : SystemMessageProvider {
                        override fun getSystemMessage(chatMemoryID: Any): String =
                            "You are helpful assistant"
                    },
                ).chatLanguageModel(model)
                .build()

        val response = assistant.askQuestion("How are you")
        assertThat(response).isEqualTo("I'm fine, thanks")
    }

    private interface Assistant {
        fun askQuestion(
            @UserMessage question: String,
        ): String
    }
}
