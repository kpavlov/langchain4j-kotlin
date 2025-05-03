package me.kpavlov.langchain4j.kotlin.service

import dev.langchain4j.internal.Exceptions
import dev.langchain4j.model.input.structured.StructuredPrompt
import dev.langchain4j.model.input.structured.StructuredPromptProcessor
import dev.langchain4j.service.IllegalConfigurationException
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.UserName
import dev.langchain4j.service.V
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
@Suppress("detekt:all")
internal object ReflectionVariableResolver {

    public fun findTemplateVariables(
        template: String,
        method: Method,
        args: Array<Any?>?,
    ): MutableMap<String?, Any?> {
        if (args == null) {
            return mutableMapOf<String?, Any?>()
        }
        val parameters = method.getParameters()

        val variables: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        for (i in args.indices) {
            val variableName = getVariableName(parameters[i])
            val variableValue = args[i]
            variables.put(variableName, variableValue)
        }

        if (template.contains("{{it}}") && !variables.containsKey("it")) {
            val itValue = getValueOfVariableIt(parameters, args)
            variables.put("it", itValue)
        }

        return variables
    }

    private fun getVariableName(parameter: Parameter): String? {
        val annotation = parameter.getAnnotation<V?>(V::class.java)
        if (annotation != null) {
            return annotation.value
        } else {
            return parameter.getName()
        }
    }

    private fun getValueOfVariableIt(
        parameters: Array<Parameter>,
        args: Array<Any?>?,
    ): String? {
        if (args != null) {
            if (args.size == 1) {
                val parameter = parameters[0]
                if (!parameter.isAnnotationPresent(MemoryId::class.java) &&
                    !parameter.isAnnotationPresent(
                        UserMessage::class.java,
                    ) &&
                    !parameter.isAnnotationPresent(
                        UserName::class.java,
                    ) &&
                    (
                        !parameter.isAnnotationPresent(V::class.java) ||
                            isAnnotatedWithIt(
                                parameter,
                            )
                    )
                ) {
                    return asString(args[0])
                }
            }

            for (i in args.indices) {
                if (isAnnotatedWithIt(parameters[i])) {
                    return asString(args[i])
                }
            }
        }

        throw IllegalConfigurationException.illegalConfiguration(
            "Error: cannot find the value of the prompt template variable \"{{it}}\".",
        )
    }

    private fun isAnnotatedWithIt(parameter: Parameter): Boolean {
        val annotation = parameter.getAnnotation<V?>(V::class.java)
        return annotation != null && "it" == annotation.value
    }

    public fun asString(arg: Any?): String? {
        if (arg == null) {
            return "null"
        } else if (arg is Array<*>?) {
            return arrayAsString(arg)
        } else if (arg.javaClass.isAnnotationPresent(StructuredPrompt::class.java)) {
            return StructuredPromptProcessor.toPrompt(arg).text()
        } else {
            return arg.toString()
        }
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
    ): Optional<String> {
        if (parameters != null &&
            parameters.size == 1 &&
            parameters[0].getAnnotations().size == 0
        ) {
            return Optional.ofNullable<String>(asString(args[0]))
        }
        return Optional.empty()
    }

    fun findUserName(
        parameters: Array<Parameter>,
        args: Array<Any?>,
    ): Optional<String> {
        for (i in parameters.indices) {
            if (parameters[i].isAnnotationPresent(UserName::class.java)) {
                return Optional.of<String>(args[i].toString())
            }
        }
        return Optional.empty<String>()
    }

    fun findMemoryId(method: Method, args: Array<Any?>?): Optional<ChatMemoryId> {
        if (args == null) {
            return Optional.empty<ChatMemoryId>()
        }
        for (i in args.indices) {
            val parameter = method.parameters[i]
            if (parameter.isAnnotationPresent(MemoryId::class.java)) {
                val memoryId = args[i]
                if (memoryId == null) {
                    throw Exceptions.illegalArgument(
                        "The value of parameter '%s' annotated with @MemoryId in method '%s' must not be null",
                        parameter.getName(), method.getName()
                    )
                }
                return Optional.of(memoryId)
            }
        }
        return Optional.empty()
    }
}
