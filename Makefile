build:
	  mvn clean verify site

apidocs:
	  mvn clean dokka:dokka -pl !reports,!samples && \
    mkdir -p target/docs && \
		cp -R langchain4j-kotlin/target/dokka target/docs/api

lint:prepare
	  ktlint && \
    mvn spotless:check

# https://docs.openrewrite.org/recipes/maven/bestpractices
format:prepare
	  ktlint --format && \
  	mvn spotless:apply && \
	  mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
				-Drewrite.activeRecipes=org.openrewrite.maven.BestPractices \
				-Drewrite.exportDatatables=true

prepare:
	  brew install ktlint --quiet

all: format lint build

