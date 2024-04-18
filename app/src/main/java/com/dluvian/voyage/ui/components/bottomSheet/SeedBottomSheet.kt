package com.dluvian.voyage.ui.components.bottomSheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedBottomSheet(seed: List<String>, onLoadSeed: Fn, onDismiss: Fn) {
    LaunchedEffect(key1 = Unit) {
        onLoadSeed()
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BottomSheetColumn(header = stringResource(id = R.string.recovery_phrase)) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(count = 3),
                contentPadding = PaddingValues(spacing.bigScreenEdge)
            ) {
                itemsIndexed(seed) { i, word ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${i + 1}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(spacing.medium))
                        Text(text = word, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
