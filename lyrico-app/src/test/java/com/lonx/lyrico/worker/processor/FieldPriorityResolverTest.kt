package com.lonx.lyrico.worker.processor

import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldPriorityResolverTest {
    @Test
    fun templateOrderPrecedesGlobalOrderForItsTarget() {
        val template = FieldPriorityTemplate(
            id = "japanese",
            name = "Japanese",
            sourceOrderByTarget = mapOf(
                MetadataFieldTarget.TITLE to listOf("apple")
            )
        )

        assertEquals(
            listOf("apple", "qq", "netease"),
            FieldPriorityResolver.orderedSourceIds(
                target = MetadataFieldTarget.TITLE,
                template = template,
                globalOrder = listOf("qq", "apple", "netease"),
                availableSourceIds = setOf("apple", "qq", "netease")
            )
        )
    }

    @Test
    fun missingTargetUsesGlobalOrder() {
        val template = FieldPriorityTemplate(
            id = "japanese",
            name = "Japanese",
            sourceOrderByTarget = mapOf(
                MetadataFieldTarget.TITLE to listOf("apple")
            )
        )

        assertEquals(
            listOf("qq", "apple"),
            FieldPriorityResolver.orderedSourceIds(
                target = MetadataFieldTarget.LYRICS,
                template = template,
                globalOrder = listOf("qq", "apple"),
                availableSourceIds = setOf("apple", "qq")
            )
        )
    }

    @Test
    fun staleAndUnavailableIdsAreSkipped() {
        val template = FieldPriorityTemplate(
            id = "japanese",
            name = "Japanese",
            sourceOrderByTarget = mapOf(
                MetadataFieldTarget.TITLE to listOf("removed", "apple")
            )
        )

        assertEquals(
            listOf("apple", "qq"),
            FieldPriorityResolver.orderedSourceIds(
                target = MetadataFieldTarget.TITLE,
                template = template,
                globalOrder = listOf("qq", "disabled", "apple"),
                availableSourceIds = setOf("apple", "qq")
            )
        )
    }

    @Test
    fun nullTemplateUsesGlobalOrder() {
        assertEquals(
            listOf("qq", "apple"),
            FieldPriorityResolver.orderedSourceIds(
                target = MetadataFieldTarget.TITLE,
                template = null,
                globalOrder = listOf("qq", "apple"),
                availableSourceIds = setOf("apple", "qq")
            )
        )
    }
}
