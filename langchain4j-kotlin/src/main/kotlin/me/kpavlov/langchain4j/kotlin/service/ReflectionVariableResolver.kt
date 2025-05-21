package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.internal.Exceptions
import dev.langchain4j.model.input.structured.StructuredPrompt
import dev.langchain4j.model.input.structured.StructuredPromptProcessor
import dev.langchain4j.service.InternalReflectionVariableResolver
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserName
import me.kpavlov.langchain4j.kotlin.ChatMemoryId
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.Optional

/**
 * Utility class responsible for resolving variable names and values for prompt templates
 * by leveraging method parameters and their annotations.
 *
 *
 * This class is intended for internal use only and is designed to extract and map
 * parameter values to template variables in methods defined within AI services.
 *
 * @see https://github.com/langchain4j/langchain4j/pull/2951
 */
internal object ReflectionVariableResolver {
    public fun findTemplateVariables(
        template: String,
        method: Method,
        args: Array<Any?>?,
    ): MutableMap<String?, Any?> =
        InternalReflectionVariableResolver.findTemplateVariables(template, method, args)

    public fun asString(arg: Any?): String =
        if (arg == null) {
            "null"
        } else if (arg is Array<*>?) {
            arrayAsString(arg)
        } else if (arg.javaClass.isAnnotationPresent(StructuredPrompt::class.java)) {
            StructuredPromptProcessor.toPrompt(arg).text()
        } else {
            arg.toString()
        }

    private fun arrayAsString(arg: Array<*>?): String =
        if (arg == null) {
            "null"
        } else {
            val sb = StringBuilder("[")
            val length = arg.size
            for (i in 0..<length) {
                sb.append(asString(arg.get(i)))
                if (i < length - 1) {
                    sb.append(", ")
                }
            }
            sb.append("]")
            sb.toString()
        }

    fun findUserMessageTemplateFromTheOnlyArgument(
        parameters: Array<Parameter>?,
        args: Array<Any?>,
    ): Optional<String> =
        if (
            parameters != null &&
            parameters.size == 1 &&
            parameters[0].getAnnotations().size == 0
        ) {
            Optional.ofNullable<String>(asString(args[0]))
        } else {
            Optional.empty()
        }


    fun findUserName(
        parameters: Array<Parameter>,
        args: Array<Any?>,
    ): Optional<String> {
        var result = Optional.empty<String>()
        for (i in args.indices) {
            if (parameters[i].isAnnotationPresent(UserName::class.java)) {
                result = Optional.of(args[i].toString())
                break
            }
        }
        return result
    }

    @Suppress("ReturnCount")
    fun findMemoryId(
        method: Method,
        args: Array<Any?>?,
    ): Optional<ChatMemoryId> {
        if (args == null) {
            return Optional.empty()
        }

        val memoryIdParam = findMemoryIdParameter(method, args)
        if (memoryIdParam != null) {
            val (parameter, memoryId) = memoryIdParam
            if (memoryId is ChatMemoryId) {
                return Optional.of(memoryId)
            } else {
                throw Exceptions.illegalArgument(
                    "The value of parameter '%s' annotated with @MemoryId in method '%s' must not be null",
                    parameter.getName(),
                    method.getName(),
                )
            }
        }

        return Optional.empty()
    }

    private fun findMemoryIdParameter(method: Method, args: Array<Any?>): Pair<Parameter, Any?>? {
        for (i in args.indices) {
            val parameter = method.parameters[i]
            if (parameter.isAnnotationPresent(MemoryId::class.java)) {
                return Pair(parameter, args[i])
            }
        }
        return null
    }
}
