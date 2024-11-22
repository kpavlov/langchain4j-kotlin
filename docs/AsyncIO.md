# Parallel Document Processing

Easy-to-use Kotlin extensions for parallel document processing using coroutines.

## Single Document Loading

```kotlin
suspend fun loadDocument() {
  val source = FileSystemSource(Paths.get("path/to/document.txt"))
  val document = loadAsync(source, TextDocumentParser())
  println(document.text())
}
```

## Load Multiple Documents in Parallel

```kotlin
suspend fun loadDocuments() {
  try {
    // Get all files from directory
    val paths =
      Files
        .walk(Paths.get("./data"))
        .filter(Files::isRegularFile)
        .toList()

    // Process each file in parallel
    val ioScope = Dispatchers.IO.limitedParallelism(8)
    val documentParser = TextDocumentParser()
    val documents =
      paths
        .map { path ->
          async {
            try {
              loadAsync(
                source = FileSystemSource(path),
                parser = documentParser,
                dispatcher = ioScope,
              )
            } catch (e: Exception) {
              logger.error("Failed to load document: $path", e)
              null
            }
          }
        }.awaitAll()
        .filterNotNull()

    // Process loaded documents
    documents.forEach { doc -> println(doc.text()) }
  } catch (e: Exception) {
    logger.error("Failed to process documents", e)
    throw e
  }
}
```

## Parse from InputStream

```kotlin
suspend fun parseInputStream(input: InputStream) {
  val parser = TextDocumentParser()
  input.use { stream ->  // Automatically close stream
    val document = parser.parseAsync(stream)
    // Process parsed document
    println(document.text())
  }
}
```

All operations use `Dispatchers.IO` for optimal I/O performance.

## Error Handling Recommendations

- Each document load operation is isolated - if one fails, others continue
- Use try-catch blocks around individual operations to handle failures gracefully
- Always close resources using [`use`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/use.html) or
  try-with-resources
- Log errors for failed operations while allowing successful ones to proceed

## Performance Tips

### 1.Batch Size

For large directories, process files in batches to control memory usage
Recommended batch size: 100-1000 documents depending on size

### 2. Memory Management:

Release document references when no longer needed
Consider using sequence for large file sets: Files.walk().asSequence()

### 3. Resource Control

Limit parallel operations based on available CPU cores
Use limitedParallelism for I/O bounds:

```kotlin
val ioScope = Dispatchers.IO.limitedParallelism(8)
```

### 4. Large Files

Stream large files instead of loading into memory
Consider chunking large documents

Uses `Dispatchers.IO` for optimal I/O throughput.
