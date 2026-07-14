package com.lonx.lyrico.worker.processor

import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.ScoredSearchResult
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import com.lonx.lyrico.data.model.metadata.StandardPluginField

object FieldCandidateResolver {
    fun resolve(
        candidates: Collection<ScoredSearchResult>,
        targetModes: Map<MetadataFieldTarget, *>,
        template: FieldPriorityTemplate?,
        globalOrder: List<String>
    ): Map<MetadataFieldTarget, ScoredSearchResult> = resolveCandidates(
        candidates = candidates,
        targetModes = targetModes,
        template = template,
        globalOrder = globalOrder
    ).mapValues { (_, candidatesForTarget) -> candidatesForTarget.first() }

    fun resolveCandidates(
        candidates: Collection<ScoredSearchResult>,
        targetModes: Map<MetadataFieldTarget, *>,
        template: FieldPriorityTemplate?,
        globalOrder: List<String>
    ): Map<MetadataFieldTarget, List<ScoredSearchResult>> {
        val bestBySource = candidates
            .filter { it.source != null }
            .groupBy { it.source!!.id }
            .mapValues { (_, results) -> results.maxByOrNull { it.score }!! }
        val availableSourceIds = bestBySource.keys

        return targetModes.keys.mapNotNull { target ->
            val field = StandardPluginField.entries.firstOrNull { it.target == target } ?: return@mapNotNull null
            val orderedCandidates = FieldPriorityResolver.orderedSourceIds(
                target = target,
                template = template,
                globalOrder = globalOrder,
                availableSourceIds = availableSourceIds
            ).mapNotNull { sourceId -> bestBySource[sourceId] }
                .filter { candidate ->
                    target == MetadataFieldTarget.LYRICS ||
                        !candidate.result.normalizedFields()[field.key].isNullOrBlank()
                }
            if (orderedCandidates.isEmpty()) null else target to orderedCandidates
        }.toMap()
    }
}
