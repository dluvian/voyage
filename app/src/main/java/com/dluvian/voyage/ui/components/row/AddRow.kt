package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun AddRow(header: String, onClick: Fn) {
    ClickableRow(
        header = header,
        leadingContent = {
            Icon(
                modifier = Modifier.padding(vertical = spacing.large),
                imageVector = AddIcon,
                contentDescription = null
            )
        },
        onClick = onClick
    )
}