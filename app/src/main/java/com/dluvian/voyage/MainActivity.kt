package com.dluvian.voyage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dluvian.voyage.core.Core
import com.dluvian.voyage.core.viewModel.HomeViewModel
import com.dluvian.voyage.ui.VoyageApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppContainer(this.applicationContext)
        setContent {
            val homeViewModel = viewModel {
                HomeViewModel(feedProvider = appContainer.feedProvider)
            }
            val core = viewModel {
                Core(
                    homeViewModel = homeViewModel,
                    postVoter = appContainer.postVoter,
                    nostrService = appContainer.nostrService
                )
            }
            VoyageApp(core)
        }
    }
}
