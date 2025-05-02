build:
	mvn clean verify site -Prelease -Dgpg.skip

apidocs:
		mvn clean dokka:dokka -pl !reports && \
		mkdir -p target/docs && \
		cp -R langchain4j-kotlin/target/dokka target/docs/api

lint:prepare
		ktlint && \
		mvn spotless:check detekt:check

# https://docs.openrewrite.org/recipes/maven/bestpractices
format:prepare
	  ktlint --format && \
		mvn spotless:apply && \
	  mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
				-Drewrite.activeRecipes=org.openrewrite.maven.BestPractices \
				-Drewrite.exportDatatables=true

prepare:
	  @if ! command -v ktlint &> /dev/null; then brew install ktlint --quiet; fi

all: format lint build

