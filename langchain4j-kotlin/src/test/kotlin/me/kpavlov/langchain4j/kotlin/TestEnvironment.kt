package me.kpavlov.langchain4j.kotlin

object TestEnvironment : me.kpavlov.finchly.BaseTestEnvironment(
    dotEnvFileDir = "../",
) {
    val openaiApiKey = TestEnvironment.get("OPENAI_API_KEY", "demo")
}
