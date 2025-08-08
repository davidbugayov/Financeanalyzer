package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Поле для комментария без иконки прикрепления
 */
@Composable
fun CommentField(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(UiR.dimen.comment_field_padding_horizontal),
                    vertical = dimensionResource(UiR.dimen.comment_field_padding_vertical),
                ),
        shape = RoundedCornerShape(dimensionResource(UiR.dimen.comment_field_corner_radius)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = dimensionResource(UiR.dimen.comment_field_elevation),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(UiR.dimen.comment_field_inner_padding_horizontal),
                        vertical = dimensionResource(UiR.dimen.comment_field_inner_padding_vertical),
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NoteField(
                note = note,
                onNoteChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
