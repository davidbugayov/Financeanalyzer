package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.transaction.R

@Composable
fun NoteField(note: String, onNoteChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text(stringResource(R.string.note_optional)) },
        modifier = modifier,
        singleLine = false,
        keyboardOptions = KeyboardOptions.Default,
    )
}
