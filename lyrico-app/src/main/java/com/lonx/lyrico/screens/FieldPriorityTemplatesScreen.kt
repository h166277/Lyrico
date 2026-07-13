package com.lonx.lyrico.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.lyrico.R
import com.lonx.lyrico.ui.components.base.YesNoDialog
import com.lonx.lyrico.ui.components.scaffoldContentPadding
import com.lonx.lyrico.viewmodel.FieldPriorityTemplatesViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.FieldPriorityTemplateEditorDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
@Destination<RootGraph>(route = "field_priority_templates")
fun FieldPriorityTemplatesScreen(navigator: DestinationsNavigator) {
    val viewModel: FieldPriorityTemplatesViewModel = koinViewModel()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var createDialog by rememberSaveable { mutableStateOf(false) }
    var nameInput by rememberSaveable { mutableStateOf("") }
    var renameId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = stringResource(R.string.field_priority_templates_title),
                navigationIcon = {
                    IconButton(onClick = navigator::navigateUp) {
                        top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                    }
                },
                actions = {
                    IconButton(onClick = { nameInput = ""; createDialog = true }) {
                        top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Add, null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = scaffoldContentPadding(padding, horizontalExtra = 12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.field_priority_templates_summary),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            if (templates.isEmpty()) {
                item { Text(stringResource(R.string.field_priority_templates_empty)) }
            }
            items(templates, key = { it.id }) { template ->
                Card(modifier = Modifier.padding(bottom = 8.dp)) {
                    ArrowPreference(
                        title = template.name,
                        summary = stringResource(R.string.field_priority_template_fields, template.sourceOrderByTarget.size),
                        onClick = { navigator.navigate(FieldPriorityTemplateEditorDestination(template.id)) },
                        endActions = {
                            IconButton(onClick = { nameInput = template.name; renameId = template.id }) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Rename, null)
                            }
                            IconButton(onClick = { deleteId = template.id }) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Delete, null)
                            }
                        }
                    )
                }
            }
        }
    }

    TemplateNameDialog(
        show = createDialog || renameId != null,
        title = stringResource(if (renameId == null) R.string.field_priority_template_create else R.string.field_priority_template_rename),
        value = nameInput,
        onValueChange = { nameInput = it },
        onDismiss = { createDialog = false; renameId = null },
        onConfirm = {
            val id = renameId
            if (id == null) viewModel.create(nameInput) else viewModel.rename(id, nameInput)
            createDialog = false
            renameId = null
        }
    )
    YesNoDialog(
        show = deleteId != null,
        summary = stringResource(R.string.field_priority_template_delete_message),
        onDismissRequest = { deleteId = null },
        onConfirm = { deleteId?.let(viewModel::delete); deleteId = null }
    )
}

@Composable
internal fun TemplateNameDialog(
    show: Boolean,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    WindowDialog(show = show, title = title, onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            top.yukonga.miuix.kmp.basic.TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.padding(top = 12.dp)
            )
            androidx.compose.material3.TextButton(onClick = onConfirm, enabled = value.isNotBlank()) {
                androidx.compose.material3.Text(stringResource(R.string.confirm))
            }
        }
    }
}
