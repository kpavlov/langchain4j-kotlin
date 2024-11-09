# langchain4j-kotlin
[![Maven Central](https://img.shields.io/maven-central/v/me.kpavlov.langchain4j.kotlin/langchain4j-kotlin-aggregator?labelColor=2a2f35)](https://repo1.maven.org/maven2/me/kpavlov/langchain4j/kotlin/)
[![Kotlin CI with Maven](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/kpavlov/langchain4j-kotlin/actions/workflows/maven.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/644f664ad05a4a009b299bc24c8be4b8)](https://app.codacy.com/gh/kpavlov/langchain4j-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)

Kotlin enhancements for [LangChain4j](https://github.com/langchain4j/langchain4j).

See the [discussion](https://github.com/langchain4j/langchain4j/discussions/1897) on LangChain4j project.

## How to run

Create `.env` file in root directory and add your API keys:

```dotenv
OPENAI_API_KEY=sk-xxxxx
```

Building project locally:
```shell
mvn clean verify
```
or
```shell
make build
```

## Acknowledgements

Training data from Project Gutenberg:

- [CAPTAIN BLOOD By Rafael Sabatini](https://www.gutenberg.org/cache/epub/1965/pg1965.txt)
