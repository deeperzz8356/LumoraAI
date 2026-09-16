package com.deep.lumoraai.feature.result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Flag

@Composable
fun ReportResultDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Result<Unit>) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var details by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val lime = Color(0xFFD4FF3B)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111A2B),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = { Icon(TablerIcons.Flag, null, tint = lime) },
        title = { Text("Report result") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Why are you reporting this result?")
                Column(Modifier.selectableGroup()) {
                    listOf("Poor quality", "Incorrect result", "Inappropriate content", "Technical issue", "Other").forEach { reason ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .selectable(selected == reason, role = Role.RadioButton, onClick = { selected = reason; error = null }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected == reason, onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = lime, unselectedColor = Color.LightGray))
                            Text(reason, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                OutlinedTextField(
                    value = details, onValueChange = { details = it },
                    label = { Text("Details (optional)") }, minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = lime, unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = lime, unfocusedLabelColor = Color.LightGray,
                    ),
                )
                error?.let { Text(it, color = Color(0xFFFFB4AB), modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            TextButton(enabled = selected != null, onClick = {
                selected?.let { reason ->
                    onSubmit(reason, details.trim()).onFailure { error = "Could not submit report. Please try again." }
                }
            }) { Text("Submit report", color = if (selected != null) lime else Color.Gray) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.LightGray) } },
    )
}
