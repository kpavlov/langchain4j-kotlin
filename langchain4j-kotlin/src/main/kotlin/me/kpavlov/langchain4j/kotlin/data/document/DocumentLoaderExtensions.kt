package me.kpavlov.langchain4j.kotlin.data.document

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentLoader
import dev.langchain4j.data.document.DocumentParser
import dev.langchain4j.data.document.DocumentSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Asynchronously loads a document from the specified source using a given parser.
 *
 * @param source The DocumentSource from which the document will be loaded.
 * @param parser The DocumentParser to parse the loaded document.
 * @param dispatcher The CoroutineDispatcher to use for asynchronous execution,
 *                  defaults to `Dispatchers.IO`.
 * @return The loaded and parsed Document.
 */
public suspend fun loadAsync(
    source: DocumentSource,
    parser: DocumentParser,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Document =
    withContext(dispatcher) {
        DocumentLoader.load(source, parser)
    }

/**
 * Asynchronously parses a document from the provided input stream using the specified dispatcher.
 *
 * @param input The InputStream from which the document will be parsed.
 * @param dispatcher The CoroutineDispatcher to use for asynchronous execution,
 *                  defaults to `Dispatchers.IO`.
 * @return The parsed Document.
 */
public suspend fun DocumentParser.parseAsync(
    input: InputStream,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Document =
    withContext(dispatcher) {
        parse(input)
    }
