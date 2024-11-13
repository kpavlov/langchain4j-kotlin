package me.kpavlov.langchain4j.kotlin

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.source.FileSystemSource
import org.slf4j.Logger
import java.nio.file.Paths

suspend fun loadDocument(
    documentName: String,
    logger: Logger,
): Document {
    val source = FileSystemSource(Paths.get("./src/test/resources/data/$documentName"))
    val document = DocumentLoader.load(source, TextDocumentParser())

    with(document) {
        logger.info("Document Metadata: {}", metadata())
        logger.info("Document Text: {}", text())
    }
    return document
}
