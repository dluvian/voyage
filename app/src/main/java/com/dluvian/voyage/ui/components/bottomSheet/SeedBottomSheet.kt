package com.dluvian.voyage.ui.components.bottomSheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.ui.components.indicator.FullLinearProgressIndicator
import com.dluvian.voyage.ui.components.text.IndexedText
import com.dluvian.voyage.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedBottomSheet(seed: List<String>, onLoadSeed: () -> Unit, onDismiss: () -> Unit) {
    LaunchedEffect(key1 = Unit) {
        onLoadSeed()
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BottomSheetColumn(header = stringResource(id = R.string.recovery_phrase)) {
            if (seed.isEmpty()) FullLinearProgressIndicator()
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(count = 3),
                contentPadding = PaddingValues(spacing.bigScreenEdge)
            ) {
                itemsIndexed(seed) { i, word ->
                    IndexedText(modifier = Modifier.fillMaxWidth(), index = i + 1, text = word)
                }
            }
        }
    }
}
