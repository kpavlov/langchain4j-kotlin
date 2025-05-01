package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.internal.Utils
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.model.chat.Capability
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ChatRequestParameters
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.chat.request.json.JsonSchema
import dev.langchain4j.model.moderation.Moderation
import dev.langchain4j.model.output.Response
import dev.langchain4j.rag.AugmentationRequest
import dev.langchain4j.rag.AugmentationResult
import dev.langchain4j.rag.query.Metadata
import dev.langchain4j.service.AiServiceContext
import dev.langchain4j.service.AiServiceTokenStream
import dev.langchain4j.service.AiServiceTokenStreamParameters
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.ChatMemoryAccess
import dev.langchain4j.service.DefaultAiServicesOpener
import dev.langchain4j.service.Moderate
import dev.langchain4j.service.Result
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.TypeUtils
import dev.langchain4j.service.memory.ChatMemoryService
import dev.langchain4j.service.output.ServiceOutputParser
import dev.langchain4j.spi.services.TokenStreamAdapter
import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@ApiStatus.Internal
internal class ServiceInvocationHandler<T : Any>(
    private val context: AiServiceContext,
    private val serviceOutputParser: ServiceOutputParser,
    private val tokenStreamAdapters: Collection<TokenStreamAdapter>
) : InvocationHandler {
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val helper = DefaultAiServicesOpener<T>(context)

    @Throws(Exception::class)
    override fun invoke(proxy: Any?, method: Method, args: Array<Any?>): Any? {
        if (method.declaringClass == Any::class.java) {
            // methods like equals(), hashCode() and toString() should not be handled by this proxy
            return method.invoke(this, *args)
        }

        if (method.declaringClass == ChatMemoryAccess::class.java) {
            return when (method.name) {
                "getChatMemory" -> context.chatMemoryService.getChatMemory(args[0])
                "evictChatMemory" -> context.chatMemoryService.evictChatMemory(args[0]) != null
                else -> throw UnsupportedOperationException(
                    "Unknown method on ChatMemoryAccess class: ${method.name}"
                )
            }
        }

        helper.validateParameters(method)

        val memoryId = helper.findMemoryId(method, args).orElse(ChatMemoryService.DEFAULT)
        val chatMemory = if (context.hasChatMemory())
            context.chatMemoryService.getOrCreateChatMemory(memoryId)
        else
            null

        val systemMessage = helper.prepareSystemMessage(memoryId, method, args)
        var userMessage = helper.prepareUserMessage(method, args)
        var augmentationResult: AugmentationResult? = null

        context.retrievalAugmentor?.let {
            val chatMemoryMessages = chatMemory?.messages()
            val metadata = Metadata.from(userMessage, memoryId, chatMemoryMessages)
            val augmentationRequest = AugmentationRequest(userMessage, metadata)
            augmentationResult = it.augment(augmentationRequest)
            userMessage = augmentationResult?.chatMessage() as UserMessage
        }

        val returnType = ReflectionHelper.getSuspendReturnType(method)
        val streaming = returnType == TokenStream::class.java || canAdaptTokenStreamTo(returnType)
        val supportsJsonSchema = supportsJsonSchema()
        var jsonSchema: JsonSchema? = null

        if (supportsJsonSchema && !streaming) {
            jsonSchema = serviceOutputParser.jsonSchema(returnType).orElse(null)
        }

        if ((jsonSchema == null) && !streaming) {
            // TODO append after storing in the memory?
            userMessage = appendOutputFormatInstructions(returnType, userMessage)
        }

        val messages = if (chatMemory != null) {
            systemMessage?.let(chatMemory::add)
            chatMemory.add(userMessage)
            chatMemory.messages()
        } else {
            mutableListOf<ChatMessage?>().apply {
                systemMessage?.let(this::add)
                add(userMessage)
            }
        }

        val moderationFuture = triggerModerationIfNeeded(method, messages)

        val toolExecutionContext =
            context.toolService.executionContext(memoryId, userMessage)

        return if (streaming) {
            handleStreamingCall(
                returnType,
                messages,
                toolExecutionContext,
                augmentationResult,
                memoryId
            )
        } else {
            handleNonStreamingCall(
                returnType,
                messages,
                toolExecutionContext,
                augmentationResult,
                moderationFuture,
                chatMemory,
                memoryId,
                supportsJsonSchema,
                jsonSchema
            )
        }
    }

    private fun handleStreamingCall(
        returnType: Type,
        messages: MutableList<ChatMessage?>,
        toolExecutionContext: dev.langchain4j.service.tool.ToolExecutionContext,
        augmentationResult: AugmentationResult?,
        memoryId: Any
    ): Any? {
        val tokenStream = AiServiceTokenStream(
            AiServiceTokenStreamParameters.builder()
                .messages(messages)
                .toolSpecifications(toolExecutionContext.toolSpecifications())
                .toolExecutors(toolExecutionContext.toolExecutors())
                .retrievedContents(augmentationResult?.contents())
                .context(context)
                .memoryId(memoryId)
                .build()
        )
        // TODO moderation
        return when {
            returnType == TokenStream::class.java -> tokenStream
            else -> adapt(tokenStream, returnType)
        }
    }

    private fun handleNonStreamingCall(
        returnType: Type,
        messages: MutableList<ChatMessage?>,
        toolExecutionContext: dev.langchain4j.service.tool.ToolExecutionContext,
        augmentationResult: AugmentationResult?,
        moderationFuture: Future<Moderation?>?,
        chatMemory: ChatMemory?,
        memoryId: ChatMemoryId,
        supportsJsonSchema: Boolean,
        jsonSchema: JsonSchema?
    ): Any? {
        val responseFormat = if (supportsJsonSchema && jsonSchema != null) {
            ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(jsonSchema)
                .build()
        } else null

        val parameters = ChatRequestParameters.builder()
            .toolSpecifications(toolExecutionContext.toolSpecifications())
            .responseFormat(responseFormat)
            .build()

        val chatRequest = ChatRequest.builder()
            .messages(messages)
            .parameters(parameters)
            .build()

        var chatResponse = context.chatModel.chat(chatRequest)

        AiServices.verifyModerationIfNeeded(moderationFuture)

        val toolExecutionResult = context.toolService.executeInferenceAndToolsLoop(
            chatResponse,
            parameters,
            messages,
            context.chatModel,
            chatMemory,
            memoryId,
            toolExecutionContext.toolExecutors()
        )

        chatResponse = toolExecutionResult.chatResponse()
        val finishReason = chatResponse.metadata().finishReason()
        val response = Response.from<AiMessage?>(
            chatResponse.aiMessage(), toolExecutionResult.tokenUsageAccumulator(), finishReason
        )

        val parsedResponse = serviceOutputParser.parse(response, returnType)
        return if (TypeUtils.typeHasRawClass(returnType, Result::class.java)) {
            Result.builder<Any?>()
                .content(parsedResponse)
                .tokenUsage(toolExecutionResult.tokenUsageAccumulator())
                .sources(augmentationResult?.contents())
                .finishReason(finishReason)
                .toolExecutions(toolExecutionResult.toolExecutions())
                .build()
        } else {
            parsedResponse
        }
    }

    private fun canAdaptTokenStreamTo(returnType: Type): Boolean =
        tokenStreamAdapters.any { it.canAdaptTokenStreamTo(returnType) }

    private fun adapt(tokenStream: TokenStream, returnType: Type): Any? =
        tokenStreamAdapters.firstOrNull { it.canAdaptTokenStreamTo(returnType) }
            ?.adapt(tokenStream)
            ?: throw IllegalStateException("Can't find suitable TokenStreamAdapter")

    private fun supportsJsonSchema(): Boolean =
        context.chatModel?.supportedCapabilities()
            ?.contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA) ?: false

    private fun appendOutputFormatInstructions(
        returnType: Type,
        userMessage: UserMessage
    ): UserMessage {
        val outputFormatInstructions = serviceOutputParser.outputFormatInstructions(returnType)
        val text = userMessage.singleText() + outputFormatInstructions
        return if (Utils.isNotNullOrBlank(userMessage.name())) UserMessage.from(userMessage.name(), text)
        else UserMessage.from(text)
    }

    private fun triggerModerationIfNeeded(
        method: Method,
        messages: MutableList<ChatMessage?>
    ): Future<Moderation?>? =
        if (method.isAnnotationPresent(Moderate::class.java)) {
            executor.submit(Callable<Moderation?> {
                val messagesToModerate = AiServices.removeToolMessages(messages)
                context.moderationModel
                    .moderate(messagesToModerate)
                    .content()
            })
        } else null
}
