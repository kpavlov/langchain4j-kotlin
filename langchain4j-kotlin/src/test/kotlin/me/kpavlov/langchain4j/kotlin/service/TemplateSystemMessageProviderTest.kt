package me.kpavlov.langchain4j.kotlin.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import me.kpavlov.langchain4j.kotlin.prompt.PromptTemplate
import me.kpavlov.langchain4j.kotlin.prompt.PromptTemplateSource
import me.kpavlov.langchain4j.kotlin.prompt.TemplateRenderer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class TemplateSystemMessageProviderTest {
    @Mock
    lateinit var templateSourceMock: PromptTemplateSource

    @Mock
    lateinit var templateRenderer: TemplateRenderer

    @Mock
    lateinit var promptTemplate: PromptTemplate

    @Mock
    lateinit var chatMemoryId: ChatMemoryId

    lateinit var templateName: String

    lateinit var subject: TemplateSystemMessageProvider

    @BeforeEach
    fun beforeEach() {
        templateName = "templateName-${System.currentTimeMillis()}"
        subject = TemplateSystemMessageProvider(templateName, templateSourceMock, templateRenderer)
    }

    @Test
    fun `getSystemMessage returns expected message when template exists`() {
        whenever(templateSourceMock.getTemplate(templateName)).thenReturn(promptTemplate)
        whenever(promptTemplate.content()).thenReturn("content")
        whenever(templateRenderer.render(eq("content"), any())).thenReturn("result")

        val result = subject.getSystemMessage(chatMemoryId)

        assertThat(result).isEqualTo("result")
    }

    @Test
    fun `getSystemMessage throws IllegalArgumentException when template is not found`() {
        whenever(templateSourceMock.getTemplate(templateName)).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            subject.getSystemMessage(
                ChatMemoryId(),
            )
        }
    }
}
