package com.lonx.lyrico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.lyrico.data.model.FieldPriorityTemplate
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import com.lonx.lyrico.data.repository.SettingsRepository
import com.lonx.lyrico.plugin.source.SearchSourceProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class FieldPriorityTemplatesViewModel(
    private val settingsRepository: SettingsRepository,
    sourceProvider: SearchSourceProvider
) : ViewModel() {
    val templates: StateFlow<List<FieldPriorityTemplate>> = settingsRepository.fieldPriorityTemplates
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val sources = sourceProvider.observeAllSources()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun create(name: String): String? {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return null
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            settingsRepository.saveFieldPriorityTemplates(
                templates.value + FieldPriorityTemplate(id = id, name = normalizedName)
            )
        }
        return id
    }

    fun rename(templateId: String, name: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return
        save(templates.value.map { if (it.id == templateId) it.copy(name = normalizedName) else it })
    }

    fun duplicate(templateId: String): String? {
        val source = templates.value.firstOrNull { it.id == templateId } ?: return null
        val id = UUID.randomUUID().toString()
        save(templates.value + source.copy(id = id, name = "${source.name} Copy"))
        return id
    }

    fun delete(templateId: String) {
        viewModelScope.launch { settingsRepository.deleteFieldPriorityTemplate(templateId) }
    }

    fun updateOrder(templateId: String, target: MetadataFieldTarget, sourceIds: List<String>) {
        save(templates.value.map { template ->
            if (template.id == templateId) {
                template.copy(sourceOrderByTarget = template.sourceOrderByTarget + (target to sourceIds.distinct()))
            } else template
        })
    }

    fun updateFieldSources(
        templateId: String,
        target: MetadataFieldTarget,
        sourceIds: List<String>,
        excludedSourceIds: Set<String>
    ) {
        save(templates.value.map { template ->
            if (template.id == templateId) {
                template.copy(
                    sourceOrderByTarget = template.sourceOrderByTarget + (target to sourceIds.distinct()),
                    excludedSourceIdsByTarget = template.excludedSourceIdsByTarget +
                        (target to excludedSourceIds)
                )
            } else template
        })
    }

    private fun save(updated: List<FieldPriorityTemplate>) {
        viewModelScope.launch { settingsRepository.saveFieldPriorityTemplates(updated) }
    }
}
