package me.kpavlov.langchain4j.kotlin.data.document

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import dev.langchain4j.data.document.DocumentSource
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.source.FileSystemSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

internal class AsyncDocumentLoaderTest {
    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)
    private lateinit var documentSource: DocumentSource
    private val parser = TextDocumentParser()

    @BeforeEach
    fun beforeEach() {
        documentSource =
            FileSystemSource(
                Paths.get("./src/test/resources/data/books/captain-blood.txt"),
            )
    }

    @Test
    fun `Should load documents asynchronously`() =
        runTest {
            val document = loadAsync(documentSource, parser)
            assertThat(document.text()).contains("Captain Blood")
            assertThat(document.metadata()).isNotNull()
        }

    @Test
    fun `Should parse documents asynchronously`() =
        runTest {
            val document = parser.parseAsync(documentSource.inputStream())
            assertThat(document.text()).contains("Captain Blood")
            assertThat(document.metadata()).isNotNull()
        }

    @Test
    fun `Should load all documents asynchronously`() =
        runTest {
            val path = Paths.get("./src/test/resources/data")
            val paths =
                Files
                    .walk(path)
                    .filter { Files.isRegularFile(it) }
                    .toList()

            // Process each file in parallel
            val ioScope = Dispatchers.IO.limitedParallelism(10)
            val documents =
                paths
                    .map { path ->
                        async {
                            try {
                                loadAsync(
                                    source = FileSystemSource(path),
                                    parser = parser,
                                    dispatcher = ioScope,
                                )
                            } catch (e: Exception) {
                                logger.error("Failed to load document: $path", e)
                                null
                            }
                        }
                    }.awaitAll()
                    .filterNotNull()

            documents.forEach {
                assertThat(it.text()).isNotEmpty()
                assertThat(it.metadata()).isNotNull()
            }
        }
}
