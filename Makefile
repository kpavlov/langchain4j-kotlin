.PHONY: build build-with-samples apidocs format lint prepare all # always run

.PHONY: build # always run
build:
	mvn --version
	mvn clean verify site -Prelease -Dgpg.skip

.PHONY: build-samples # always run
build-samples:
	VERSION=$$(grep -m1 "<version>" pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | xargs) && \
	echo -e "Project version is $$VERSION" && \
	mvn install -DskipTests && \
	(cd samples && mvn -B clean test -Dlangchain4j-kotlin.version=$$VERSION)

.PHONY: apidocs # always run
apidocs:
		mvn clean dokka:dokka -pl !reports && \
		mkdir -p target/docs && \
		cp -R langchain4j-kotlin/target/dokka target/docs/api

.PHONY: lint # always run
lint:prepare
		ktlint && \
		mvn spotless:check detekt:check

.PHONY: format # always run
# https://docs.openrewrite.org/recipes/maven/bestpractices
format:prepare
	  ktlint --format && \
		mvn spotless:apply && \
	  mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
				-Drewrite.activeRecipes=org.openrewrite.maven.BestPractices \
				-Drewrite.exportDatatables=true

.PHONY: prepare # always run
prepare:
	  @if ! command -v ktlint &> /dev/null; then brew install ktlint --quiet; fi

.PHONY: all # always run
all: format lint build

