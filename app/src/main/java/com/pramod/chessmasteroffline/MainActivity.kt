package com.pramod.chessmasteroffline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pramod.chessmasteroffline.ui.ChessMasterApp
import com.pramod.chessmasteroffline.ui.ChessViewModel
import com.pramod.chessmasteroffline.ui.theme.ChessMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessMasterTheme {
                val viewModel: ChessViewModel = viewModel()
                ChessMasterApp(viewModel = viewModel)
            }
        }
    }
}
