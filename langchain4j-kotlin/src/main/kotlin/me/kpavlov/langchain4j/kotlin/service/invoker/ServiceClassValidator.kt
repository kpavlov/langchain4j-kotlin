package me.kpavlov.langchain4j.kotlin.service.invoker

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Utility for validating AiServices interface and their methods
 * at the time of AiService proxy construction.
 */
public object ServiceClassValidator {
    private val logger = LoggerFactory.getLogger(ServiceClassValidator::class.java)

    public fun <T : Any> validateClass(serviceClass: KClass<T>) {
        serviceClass.members
            .filter { it is KFunction }
            .map { it as KFunction }
            .forEach { method ->
                logger.info(
                    "Discovered method: {} ({})${method.name} : {}",
                    method.name,
                    method.parameters,
                    method.returnType,
                )
                validateMethod(method)
            }
    }

    public fun validateMethod(method: KFunction<*>) {
        logger.debug("Validating method: {}", method)
        // real validation
        logger.debug("Method: {} is good", method)
    }
}
