# Development Guidelines

## Code Style

### Kotlin

- Follow Kotlin coding conventions
- Use the provided `.editorconfig` for consistent formatting
- Use Kotlin typesafe DSL builders where possible and prioritize fluent builders style over standard builder methods.
  If DSL builders produce less readable code, use standard setter methods.
- Use Kotlin's `val` for immutable properties and `var` for mutable properties
- Ensure to preserve backward compatibility when making changes

### Java

- Use the provided `.editorconfig` for consistent formatting
- For Java code prefer fluent DSL style over standard bean getters and setter methods

## Testing

- Write comprehensive tests for new features
- Write Kotlin tests with kotlin-test and Kotest-assertions with infix form assertions `shouldBe` instead of
  Assertj's `assertThat(...)`.
- Use Kotest's `withClue("<failure reason>")` to describe failure reasons, but only when the assertion is NOT obvious.
  Remove obvious cases for simplicity.
- Use `assertSoftly(subject) { ... }` to perform multiple assertions. Never use `assertSoftly { }` to verify properties
  of different subjects, or when there is only one assertion per subject. Avoid using `assertSoftly(this) { ... }`
- Prioritize test readability
- When asked to write tests in Java: use JUnit5, Mockito, AssertJ core

## Documentation

- Update README files when adding new features
- Document API changes in the appropriate module's documentation
- Write tutorials in Hugo markdown /docs/content/docs

### Project Documentation

The project uses two main tools for documentation:

1. **Dokka** - For API documentation generation from code
2. **Hugo** - For building the documentation website

### Building the Project

```shell
gradle build
```

or using Make:

```shell
make
```
