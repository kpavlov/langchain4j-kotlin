package me.kpavlov.langchain4j.kotlin.prompt

import me.kpavlov.langchain4j.kotlin.TemplateContent

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
