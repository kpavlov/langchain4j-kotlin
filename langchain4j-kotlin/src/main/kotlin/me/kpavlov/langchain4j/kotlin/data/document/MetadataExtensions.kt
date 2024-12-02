package me.kpavlov.langchain4j.kotlin.data.document

import dev.langchain4j.data.document.Metadata

/**
 * Merges the current Metadata object with another Metadata object.
 * The two Metadata objects must not have any common keys.
 *
 * @param another The Metadata object to be merged with the current Metadata object.
 * @return A new Metadata object that contains all key-value pairs from both Metadata objects.
 * @throws IllegalArgumentException if there are common keys between the two Metadata objects.
 */
public fun Metadata.merge(another: Metadata): Metadata {
    val thisMap = this.toMap()
    val anotherMap = another.toMap()
    val commonKeys = thisMap.keys.intersect(anotherMap.keys)
    require(commonKeys.isEmpty()) {
        "Metadata keys are not unique. Common keys: $commonKeys"
    }
    return Metadata.from(thisMap.plus(anotherMap))
}
