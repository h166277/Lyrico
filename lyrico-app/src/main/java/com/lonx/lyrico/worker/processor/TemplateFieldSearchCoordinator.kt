package com.lonx.lyrico.worker.processor

import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.ScoredSearchResult
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import com.lonx.lyrico.data.model.metadata.StandardPluginField
import com.lonx.lyrico.utils.MatchScoreDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal object TemplateFieldSearchCoordinator {
    suspend fun collectCandidates(
        targetModes: Set<MetadataFieldTarget>,
        template: FieldPriorityTemplate,
        globalOrder: List<String>,
        availableSourceIds: Set<String>,
        fetchBestCandidate: suspend (String) -> Pair<ScoredSearchResult, MatchScoreDetail>?
    ): List<Pair<ScoredSearchResult, MatchScoreDetail>> = coroutineScope {
        val requestedSourceIds = mutableSetOf<String>()
        val candidates = mutableListOf<Pair<ScoredSearchResult, MatchScoreDetail>>()

        while (true) {
            val sourcesWithFieldData = sourcesWithFieldData(targetModes, candidates)
            val nextSourceIds = FieldPrioritySearchPlanner.nextSourceIds(
                targets = targetModes,
                template = template,
                globalOrder = globalOrder.filter { it in availableSourceIds },
                requestedSourceIds = requestedSourceIds,
                sourcesWithFieldData = sourcesWithFieldData
            )
            if (nextSourceIds.isEmpty()) return@coroutineScope candidates

            requestedSourceIds += nextSourceIds
            candidates += nextSourceIds.map { sourceId ->
                async { fetchBestCandidate(sourceId) }
            }.awaitAll().filterNotNull()
        }
    }

    private fun sourcesWithFieldData(
        targets: Set<MetadataFieldTarget>,
        candidates: List<Pair<ScoredSearchResult, MatchScoreDetail>>
    ): Map<MetadataFieldTarget, Set<String>> {
        return targets.associateWith { target ->
            candidates.asSequence()
                .filter { (candidate, _) ->
                    StandardPluginField.entries
                        .firstOrNull { it.target == target }
                        ?.let { field ->
                            !candidate.result.normalizedFields()[field.key].isNullOrBlank()
                        } == true
                }
                .mapNotNull { (candidate, _) -> candidate.source?.id }
                .toSet()
        }
    }
}
