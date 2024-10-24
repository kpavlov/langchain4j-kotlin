package me.kpavlov.langchain4j.kotlin

import io.github.cdimascio.dotenv.dotenv

object TestEnvironment {

    val dotenv = dotenv{
        directory = System.getProperty("user.dir")
        ignoreIfMissing = true
    }

}