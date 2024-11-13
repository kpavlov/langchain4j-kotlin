build:
	mvn clean verify site

lint:
	# brew install ktlint
	ktlint --format
  # https://docs.openrewrite.org/recipes/maven/bestpractices
  mvn rewrite:run -P lint
