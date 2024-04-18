package com.dluvian.voyage.ui.components.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.components.text.AnnotatedText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPostBottomSheet(content: AnnotatedString, onDismiss: Fn) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BottomSheetColumn(header = stringResource(id = R.string.original_post)) {
            AnnotatedText(text = content, onClick = { })
        }
    }
}
