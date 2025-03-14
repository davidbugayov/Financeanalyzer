package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R

@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick
)

@Composable
fun SourceField(
    source: String,
    color: Int,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = source,
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.source)) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.select_source)
                )
            },
            supportingText = if (isError) {
                { Text(stringResource(R.string.source_error)) }
            } else null,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
    }
} 