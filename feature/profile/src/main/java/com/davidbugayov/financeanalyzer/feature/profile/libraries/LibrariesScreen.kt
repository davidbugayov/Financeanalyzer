package com.davidbugayov.financeanalyzer.feature.profile.libraries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.feature.profile.R
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_primary
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_primaryContainer
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_secondary
import com.davidbugayov.financeanalyzer.ui.theme.md_theme_light_secondaryContainer

/**
 * Экран со списком используемых в приложении библиотек.
 * Вызывается из профиля.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.libraries_title),
                showBackButton = true,
                onBackClick = onNavigateBack,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_normal)),
        ) {
            item {
                Text(
                    text = stringResource(R.string.libraries_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_normal)),
                )
            }

            val libraries = getLibraries()
            items(libraries) { library ->
                LibraryItem(
                    name = stringResource(library.nameResId),
                    version = library.version,
                    description = stringResource(library.descriptionResId),
                    license = library.license,
                    index = libraries.indexOf(library),
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            }

            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))

                Text(
                    text = stringResource(R.string.licenses_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxlarge)))
            }
        }
    }
}

@Composable
private fun LibraryItem(
    name: String,
    version: String,
    description: String,
    license: String,
    index: Int,
) {
    val gradientColors =
        if (index % 2 == 0) {
            listOf(
                md_theme_light_primary.copy(alpha = 0.9f),
                md_theme_light_primaryContainer.copy(alpha = 0.3f),
            )
        } else {
            listOf(
                md_theme_light_secondary.copy(alpha = 0.8f),
                md_theme_light_secondaryContainer.copy(alpha = 0.3f),
            )
        }

    val textColor = Color.White

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.spacing_medium)),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = dimensionResource(R.dimen.card_elevation).div(2),
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                    )
                    .padding(dimensionResource(R.dimen.spacing_normal)),
        ) {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )

                Text(
                    text = stringResource(UiR.string.library_version_format, version),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.9f),
                )
            }
        }

        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal)),
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_normal)))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.height_divider)),
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Text(
                text = stringResource(R.string.license_colon, license),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

data class Library(
    val nameResId: Int,
    val version: String,
    val descriptionResId: Int,
    val license: String,
)

private fun getLibraries(): List<Library> {
    return listOf(
        Library(
            nameResId = UiR.string.library_jetpack_compose_name,
            version = "1.6.1",
            descriptionResId = UiR.string.library_jetpack_compose_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_kotlin_coroutines_name,
            version = "1.8.0",
            descriptionResId = UiR.string.library_kotlin_coroutines_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_koin_name,
            version = "3.5.3",
            descriptionResId = UiR.string.library_koin_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_room_name,
            version = "2.6.1",
            descriptionResId = UiR.string.library_room_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_material3_name,
            version = "1.2.0",
            descriptionResId = UiR.string.library_material3_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_timber_name,
            version = "5.0.1",
            descriptionResId = UiR.string.library_timber_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_pdfbox_android_name,
            version = "2.0.27.0",
            descriptionResId = UiR.string.library_pdfbox_android_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_apache_poi_name,
            version = "5.2.5",
            descriptionResId = UiR.string.library_apache_poi_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_navigation_compose_name,
            version = "2.7.7",
            descriptionResId = UiR.string.library_navigation_compose_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_lifecycle_name,
            version = "2.7.0",
            descriptionResId = UiR.string.library_lifecycle_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_core_ktx_name,
            version = "1.12.0",
            descriptionResId = UiR.string.library_core_ktx_description,
            license = "Apache License 2.0",
        ),
        Library(
            nameResId = UiR.string.library_accompanist_name,
            version = "0.34.0",
            descriptionResId = UiR.string.library_accompanist_description,
            license = "Apache License 2.0",
        ),
    )
}
