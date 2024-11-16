package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent

/**
 * A simple implementation of the TemplateRenderer interface that replaces placeholders
 * in the provided template with corresponding variable values.
 *
 * The placeholders in the template should be in the format `{{key}}`, where `key` corresponds
 * to an entry in the variables map.
 *
 * If any placeholders in the template are not defined in the variables map, an IllegalArgumentException
 * will be thrown.
 */
public class SimpleTemplateRenderer : TemplateRenderer {
    public override fun render(
        template: TemplateContent,
        variables: Map<String, Any?>,
    ): String {
        val undefinedKeys =
            "\\{\\{(\\w+)\\}\\}"
                .toRegex()
                .findAll(template)
                .map { it.groupValues[1] }
                .filterNot { variables.containsKey(it) }
                .toList()

        require(undefinedKeys.isEmpty()) {
            "Undefined keys in template: ${
                undefinedKeys.joinToString(
                    ", ",
                )
            }"
        }

        return variables.entries.fold(template) { acc, entry ->
            acc.replace("{{${entry.key}}}", entry.value?.toString() ?: "")
        }
    }
}
