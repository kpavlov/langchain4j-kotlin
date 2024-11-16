package me.kpavlov.langchain4j.kotlin.prompt

public object SimpleTemplateRenderer : TemplateRenderer {
    override fun render(
        template: String,
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
