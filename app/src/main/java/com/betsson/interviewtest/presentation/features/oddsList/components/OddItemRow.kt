package com.betsson.interviewtest.presentation.features.oddsList.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.betsson.interviewtest.R
import com.betsson.interviewtest.presentation.features.oddsList.OddItemUiModel

@Composable
fun OddItemRow(
    oddItem: OddItemUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp), // Less padding than full screen
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp) // Internal padding for content in the card
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(oddItem.imageUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .build(),
                contentDescription = oddItem.name,
                contentScale = ContentScale.Crop, // Or ContentScale.Fit
                modifier = Modifier.size(64.dp) // Control image size
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f) // Takes remaining space
            ) {
                Text(
                    text = oddItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = oddItem.sellInText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = oddItem.oddsValueText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OddItemRowPreview() {
    // Provide a sample OddItemUiModel for the preview
    OddItemRow(
        oddItem = OddItemUiModel(
            id = "1",
            name = "Winning Team",
            sellInText = "Sell In: 10",
            oddsValueText = "Odds: 20",
            imageUrl = "https://i.imgur.com/mx66SBD.jpeg" // A sample image URL
        )
    )
}


