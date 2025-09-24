package com.betsson.interviewtest.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.betsson.interviewtest.presentation.features.oddsList.OddsListScreen
import com.betsson.interviewtest.presentation.features.oddsList.OddsListViewModel
import com.betsson.interviewtest.presentation.theme.InterviewTestTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val oddsListViewModel: OddsListViewModel by viewModels ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InterviewTestTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Cyan), // FORCE DISTINCT BG
                    color = MaterialTheme.colorScheme.background // This will be overridden by the modifier if background is drawn after
                ) {
                    OddsListScreen(viewModel = oddsListViewModel)
                }
            }
        }
    }
}

