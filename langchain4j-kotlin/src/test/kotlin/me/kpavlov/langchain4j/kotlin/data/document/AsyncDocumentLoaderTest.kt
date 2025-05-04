package me.kpavlov.langchain4j.kotlin.data.document

import dev.langchain4j.data.document.DocumentSource
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.source.FileSystemSource
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@OptIn(ExperimentalCoroutinesApi::class)
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
            document.text() shouldContain "Captain Blood"
            document.metadata() shouldNotBe null
        }

    @Test
    fun `Should parse documents asynchronously`() =
        runTest {
            val document = parser.parseAsync(documentSource.inputStream())
            document.text() shouldContain "Captain Blood"
            document.metadata() shouldNotBe null
        }

    @Test
    fun `Should load all documents asynchronously`() =
        runTest {
            println("AsyncDocumentLoaderTest.Should load all documents asynchronously")
            val rootPath = Paths.get("./src/test/resources/data")
            val paths =
                Files
                    .walk(rootPath)
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
                                    context = ioScope,
                                )
                            } catch (e: Exception) {
                                logger.error("Failed to load document: $path", e)
                                null
                            }
                        }
                    }.awaitAll()
                    .filterNotNull()

            documents.forEach {
                it.text().shouldNotBeEmpty()
                it.metadata() shouldNotBe null
            }
        }

    @Test
    fun `Should loadDocumentsAsync`() =
        runTest {
            val documents =
                loadDocumentsAsync(
                    recursive = true,
                    documentParser = parser,
                    directoryPaths = listOf(Path.of("./src/test/resources/data")),
                )
            documents shouldHaveSize 3

            val documentNames = documents.map { it.metadata().getString("file_name") }
            documentNames shouldContainExactlyInAnyOrder
                listOf(
                    "captain-blood.txt",
                    "quantum-computing.txt",
                    "blumblefang.txt",
                )
        }
}
