package me.kpavlov.langchain4j.kotlin.data.document

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.langchain4j.data.document.Metadata
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class MetadataExtensionsTest {
    @Test
    fun `merge should combine unique metadata from both objects`() {
        val metadata1 =
            mock<Metadata> {
                on { toMap() } doReturn mapOf("key1" to "value1", "key2" to "value2")
            }
        val metadata2 =
            mock<Metadata> {
                on { toMap() } doReturn mapOf("key3" to "value3", "key4" to "value4")
            }

        val result = metadata1.merge(metadata2)

        assertThat(result.toMap()).isEqualTo(
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "value3",
                "key4" to "value4",
            ),
        )
    }

    @Test
    fun `merge should throw an exception when there are common keys`() {
        val metadata1 =
            mock<Metadata> {
                on { toMap() } doReturn mapOf("key1" to "value1", "key2" to "value2")
            }
        val metadata2 =
            mock<Metadata> {
                on { toMap() } doReturn mapOf("key2" to "value3", "key3" to "value4")
            }

        val exception =
            assertThrows<IllegalArgumentException> {
                metadata1.merge(metadata2)
            }

        assertThat(exception.message)
            .isEqualTo("Metadata keys are not unique. Common keys: [key2]")
    }
}
