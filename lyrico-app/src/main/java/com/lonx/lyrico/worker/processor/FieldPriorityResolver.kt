package com.lonx.lyrico.worker.processor

import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget

object FieldPriorityResolver {
    fun orderedSourceIds(
        target: MetadataFieldTarget,
        template: FieldPriorityTemplate?,
        globalOrder: List<String>,
        availableSourceIds: Set<String>
    ): List<String> {
        val excludedSourceIds = template?.excludedSourceIdsByTarget?.get(target).orEmpty()
        return (template?.sourceOrderByTarget?.get(target).orEmpty() + globalOrder)
            .distinct()
            .filter { it in availableSourceIds && it !in excludedSourceIds }
    }
}
