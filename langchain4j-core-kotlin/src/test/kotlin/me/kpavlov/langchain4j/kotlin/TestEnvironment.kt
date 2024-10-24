package me.kpavlov.langchain4j.kotlin

import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory
import java.nio.file.Paths

private val logger = LoggerFactory.getLogger(TestEnvironment.javaClass)

object TestEnvironment {

    private val dotenv = dotenv {
        directory = Paths.get("${System.getProperty("user.dir")}/..").normalize().toString()
        logger.info("Loading .env file from $directory")
        ignoreIfMissing = true
    }

    fun env(name: String) = dotenv.get(name)

}