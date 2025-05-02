package me.kpavlov.langchain4j.kotlin.rag

import dev.langchain4j.rag.AugmentationRequest
import dev.langchain4j.rag.AugmentationResult
import dev.langchain4j.rag.RetrievalAugmentor
import kotlinx.coroutines.coroutineScope

public suspend fun RetrievalAugmentor.augmentAsync(request: AugmentationRequest): AugmentationResult =
    coroutineScope { this@augmentAsync.augment(request) }
