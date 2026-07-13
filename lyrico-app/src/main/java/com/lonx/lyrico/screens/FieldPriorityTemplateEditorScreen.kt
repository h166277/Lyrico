package com.lonx.lyrico.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.lyrico.R
import com.lonx.lyrico.data.model.BatchMatchConfigDefaults
import com.lonx.lyrico.data.model.metadata.MetadataFieldTarget
import com.lonx.lyrico.data.model.lyrics.SearchSource
import com.lonx.lyrico.ui.components.scaffoldContentPadding
import com.lonx.lyrico.viewmodel.FieldPriorityTemplatesViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
@Destination<RootGraph>(route = "field_priority_template_editor")
fun FieldPriorityTemplateEditorScreen(
    templateId: String,
    navigator: DestinationsNavigator
) {
    val viewModel: FieldPriorityTemplatesViewModel = koinViewModel()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val template = templates.firstOrNull { it.id == templateId }
    var editingTarget by remember { mutableStateOf<MetadataFieldTarget?>(null) }
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = template?.name ?: stringResource(R.string.field_priority_templates_title),
                navigationIcon = {
                    IconButton(onClick = navigator::navigateUp) {
                        top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (template == null) return@Scaffold
        LazyColumn(
            modifier = Modifier.fillMaxSize().scaffoldContentPadding(padding),
            contentPadding = PaddingValues(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.field_priority_template_editor_summary),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            BatchMatchConfigDefaults.TARGET_GROUPS.forEach { group ->
                item("group_${group.titleRes}") {
                    Text(stringResource(group.titleRes), modifier = Modifier.padding(vertical = 8.dp))
                }
                items(group.targets, key = { it.name }) { target ->
                    val order = template.sourceOrderByTarget[target].orEmpty()
                    Card(modifier = Modifier.padding(bottom = 8.dp)) {
                        ArrowPreference(
                            title = stringResource(target.labelRes),
                            summary = order.mapNotNull { id -> sources.firstOrNull { it.id == id }?.name }
                                .joinToString()
                                .ifBlank { stringResource(R.string.field_priority_template_global_order) },
                            onClick = { editingTarget = target }
                        )
                    }
                }
            }
        }
    }

    editingTarget?.let { target ->
        FieldPrioritySourceOrderDialog(
            title = stringResource(target.labelRes),
            sources = sources,
            initialOrder = template?.sourceOrderByTarget?.get(target).orEmpty(),
            onDismiss = { editingTarget = null },
            onSave = { order ->
                viewModel.updateOrder(templateId, target, order)
                editingTarget = null
            }
        )
    }
}

@Composable
private fun FieldPrioritySourceOrderDialog(
    title: String,
    sources: List<SearchSource>,
    initialOrder: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var order by remember(initialOrder, sources) {
        mutableStateOf((initialOrder + sources.map { it.id }).distinct().filter { id -> sources.any { it.id == id } })
    }
    WindowDialog(show = true, title = title, onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.field_priority_template_order_summary))
            order.forEachIndexed { index, sourceId ->
                val source = sources.firstOrNull { it.id == sourceId } ?: return@forEachIndexed
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(source.name, modifier = Modifier.weight(1f))
                    androidx.compose.material3.TextButton(
                        enabled = index > 0,
                        onClick = {
                            order = order.toMutableList().apply {
                                add(index - 1, removeAt(index))
                            }
                        }
                    ) { androidx.compose.material3.Text(stringResource(R.string.field_priority_template_move_up)) }
                    androidx.compose.material3.TextButton(
                        enabled = index < order.lastIndex,
                        onClick = {
                            order = order.toMutableList().apply {
                                add(index + 1, removeAt(index))
                            }
                        }
                    ) { androidx.compose.material3.Text(stringResource(R.string.field_priority_template_move_down)) }
                }
            }
            Spacer(Modifier.padding(top = 8.dp))
            androidx.compose.material3.TextButton(onClick = { onSave(order) }) {
                androidx.compose.material3.Text(stringResource(R.string.confirm))
            }
        }
    }
}
