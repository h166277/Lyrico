package com.lonx.lyrico.worker.processor

import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldPrioritySearchPlannerTest {
    private val targets = setOf(MetadataFieldTarget.TITLE, MetadataFieldTarget.LYRICS)
    private val template = FieldPriorityTemplate(
        id = "template",
        name = "Template",
        sourceOrderByTarget = mapOf(
            MetadataFieldTarget.TITLE to listOf("apple", "qq"),
            MetadataFieldTarget.LYRICS to listOf("qq", "netease")
        )
    )

    @Test
    fun initialWaveDeduplicatesFirstPrioritySourcesAcrossFields() {
        assertEquals(
            setOf("apple", "qq"),
            FieldPrioritySearchPlanner.nextSourceIds(
                targets = targets,
                template = template,
                globalOrder = listOf("apple", "qq", "netease"),
                requestedSourceIds = emptySet(),
                sourcesWithFieldData = emptyMap()
            )
        )
    }

    @Test
    fun requestsOnlyFallbackForFieldsWithoutUsableData() {
        assertEquals(
            setOf("netease"),
            FieldPrioritySearchPlanner.nextSourceIds(
                targets = targets,
                template = template,
                globalOrder = listOf("apple", "qq", "netease"),
                requestedSourceIds = setOf("apple", "qq"),
                sourcesWithFieldData = mapOf(
                    MetadataFieldTarget.TITLE to setOf("apple"),
                    MetadataFieldTarget.LYRICS to emptySet()
                )
            )
        )
    }

    @Test
    fun stopsRequestingWhenAllFieldsHaveUsableData() {
        assertEquals(
            emptySet<String>(),
            FieldPrioritySearchPlanner.nextSourceIds(
                targets = targets,
                template = template,
                globalOrder = listOf("apple", "qq", "netease"),
                requestedSourceIds = setOf("apple", "qq"),
                sourcesWithFieldData = mapOf(
                    MetadataFieldTarget.TITLE to setOf("apple"),
                    MetadataFieldTarget.LYRICS to setOf("qq")
                )
            )
        )
    }
}
