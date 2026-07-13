package com.lonx.lyrico.worker.processor

import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.ScoredSearchResult
import com.lonx.lyrico.data.model.lyrics.SearchSource
import com.lonx.lyrico.data.model.lyrics.SongSearchResult
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldCandidateResolverTest {
    @Test
    fun titleUsesAppleWhileLyricsUsesQq() {
        val apple = source("apple")
        val qq = source("qq")
        val resolved = FieldCandidateResolver.resolve(
            candidates = listOf(
                candidate(apple, mapOf("title" to "Apple title")),
                candidate(qq, mapOf("title" to "QQ title", "lyrics" to "QQ lyrics"))
            ),
            targetModes = mapOf(MetadataFieldTarget.TITLE to Unit, MetadataFieldTarget.LYRICS to Unit),
            template = FieldPriorityTemplate(
                id = "default",
                name = "Default",
                sourceOrderByTarget = mapOf(
                    MetadataFieldTarget.TITLE to listOf("apple"),
                    MetadataFieldTarget.LYRICS to listOf("qq")
                )
            ),
            globalOrder = listOf("apple", "qq")
        )

        assertEquals("apple", resolved[MetadataFieldTarget.TITLE]?.source?.id)
        assertEquals("qq", resolved[MetadataFieldTarget.LYRICS]?.source?.id)
    }

    @Test
    fun blankPreferredValueFallsBackToNextSource() {
        val resolved = FieldCandidateResolver.resolve(
            candidates = listOf(candidate(source("apple"), emptyMap()), candidate(source("qq"), mapOf("title" to "QQ title"))),
            targetModes = mapOf(MetadataFieldTarget.TITLE to Unit),
            template = FieldPriorityTemplate("default", "Default", mapOf(MetadataFieldTarget.TITLE to listOf("apple"))),
            globalOrder = listOf("apple", "qq")
        )
        assertEquals("qq", resolved[MetadataFieldTarget.TITLE]?.source?.id)
    }

    private fun source(id: String) = object : SearchSource {
        override val id = id
        override val name = id
        override suspend fun searchSongs(keyword: String, page: Int, separator: String, pageSize: Int) = emptyList<SongSearchResult>()
        override suspend fun getLyrics(song: SongSearchResult) = null
        override suspend fun searchCovers(keyword: String, pageSize: Int) = emptyList<SongSearchResult>()
    }

    private fun candidate(source: SearchSource, fields: Map<String, String>) = ScoredSearchResult(
        result = SongSearchResult("${source.id}-song", source.id, source.name, fields = fields), score = 1.0, source = source
    )
}
