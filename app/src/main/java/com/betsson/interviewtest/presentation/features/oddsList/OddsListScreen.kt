package com.betsson.interviewtest.presentation.features.oddsList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.betsson.interviewtest.presentation.features.oddsList.components.OddItemRow

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@Composable
fun OddsListScreen(
    viewModel: OddsListViewModel // This will be provided by Koin later
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Odds") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues -> // Content padding provided by Scaffold
        OddsListContent(
            uiState = uiState,
            onUpdateClick = { viewModel.onUpdateOddsClicked() },
            modifier = Modifier.padding(paddingValues) // Apply Scaffold padding
        )
    }
}

@Composable
fun OddsListContent(
    uiState: OddsListUiState,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp) // Outer padding for the column content
    ) {
        Button(
            onClick = onUpdateClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !uiState.isLoading // Disable button while loading
        ) {
            Text("Update Odds")
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading && uiState.odds.isEmpty() -> { // Show full screen loader only on initial load
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            uiState.odds.isEmpty() && !uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No odds available at the moment.")
                }
            }
            else -> {
                // Show a small loader on top of the list if updating
                if (uiState.isLoading && uiState.odds.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp), // Space between items
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = uiState.odds,
                        key = { oddItem -> oddItem.id } // For more efficient recompositions
                    ) { oddItem ->
                        OddItemRow(oddItem = oddItem)
                    }
                }
            }
        }
    }
}


// Basic Preview for OddsListContent (can be expanded)
@Preview(showBackground = true)
@Composable
fun OddsListContentPreview_Loading() {
    MaterialTheme { // Wrap in a theme for preview
        OddsListContent(
            uiState = OddsListUiState(isLoading = true),
            onUpdateClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OddsListContentPreview_Error() {
    MaterialTheme {
        OddsListContent(
            uiState = OddsListUiState(error = "Network connection failed"),
            onUpdateClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OddsListContentPreview_Data() {
    MaterialTheme {
        OddsListContent(
            uiState = OddsListUiState(
                odds = listOf(
                    OddItemUiModel("1", "Team A vs Team B", "Sell In: 5", "Odds: 1.5", null),
                    OddItemUiModel("2", "Total Goals", "Sell In: 2", "Odds: 2.5", null)
                )
            ),
            onUpdateClick = {}
        )
    }
}


