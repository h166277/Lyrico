package com.lonx.lyrico.worker.processor

import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget

object FieldPrioritySearchPlanner {
    fun nextSourceIds(
        targets: Set<MetadataFieldTarget>,
        template: FieldPriorityTemplate,
        globalOrder: List<String>,
        requestedSourceIds: Set<String>,
        sourcesWithFieldData: Map<MetadataFieldTarget, Set<String>>
    ): Set<String> {
        return targets
            .filter { target -> sourcesWithFieldData[target].isNullOrEmpty() }
            .mapNotNull { target ->
                FieldPriorityResolver.orderedSourceIds(
                    target = target,
                    template = template,
                    globalOrder = globalOrder,
                    availableSourceIds = globalOrder.toSet()
                ).firstOrNull { sourceId -> sourceId !in requestedSourceIds }
            }
            .toSet()
    }
}
