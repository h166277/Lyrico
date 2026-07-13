package com.lonx.lyrico.data.model

import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import kotlinx.serialization.Serializable

@Serializable
data class FieldPriorityTemplate(
    val id: String,
    val name: String,
    val sourceOrderByTarget: Map<MetadataFieldTarget, List<String>> = emptyMap()
)
