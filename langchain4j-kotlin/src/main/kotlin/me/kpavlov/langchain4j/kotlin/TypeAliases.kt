package me.kpavlov.langchain4j.kotlin

/**
 * Type alias representing an identifier for a chat memory.
 *
 * This type alias is used within the `SystemMessageProvider` interface
 * and its implementations to specify the input parameter for retrieving
 * system messages.
 */
public typealias ChatMemoryId = Any

/**
 * Type alias for the name of a template.
 *
 * This alias is used to represent template names as strings in various parts
 * of the codebase, providing a clearer and more specific meaning compared
 * to using `String` directly.
 */
public typealias TemplateName = String

/**
 * Represents the content of a template.
 *
 * This type alias is used to define a standard type for template content within the system,
 * which is expected to be in the form of a string. Various classes and functions that deal
 * with templates will utilize this type alias to ensure consistency and clarity.
 */
public typealias TemplateContent = String

/**
 * Type alias for a string representing the content of a prompt.
 *
 * This alias is used to define the type of content that can be returned
 * by various functions and methods within the system that deal with
 * generating and handling prompts.
 */
public typealias PromptContent = String
