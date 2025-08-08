package com.davidbugayov.financeanalyzer.feature.transaction.base.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Кнопка добавления транзакции
 */
@Composable
fun AddButton(
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    text: String? = null,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(R.dimen.add_button_padding_horizontal),
                    vertical = dimensionResource(R.dimen.add_button_padding_vertical),
                )
                .height(dimensionResource(R.dimen.add_button_height)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.add_button_corner_radius)),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = color,
            ),
        enabled = !isLoading,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(R.dimen.add_button_progress_size)),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = dimensionResource(R.dimen.add_button_progress_stroke_width),
                )
            } else {
                Text(
                    text = text ?: stringResource(UiR.string.add),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}
