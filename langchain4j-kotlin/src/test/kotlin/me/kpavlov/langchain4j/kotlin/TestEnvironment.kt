package me.kpavlov.langchain4j.kotlin

import me.kpavlov.aimocks.openai.MockOpenai

object TestEnvironment : me.kpavlov.finchly.BaseTestEnvironment(
    dotEnvFileDir = "../",
) {
    val openaiApiKey = get("OPENAI_API_KEY", "demo")
    val mockOpenAi = MockOpenai()
}
