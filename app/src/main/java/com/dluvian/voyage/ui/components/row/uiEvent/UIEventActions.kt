package com.dluvian.voyage.ui.components.row.uiEvent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.ui.components.button.footer.BookmarkIconButton
import com.dluvian.voyage.ui.components.button.footer.CrossPostIconButton
import com.dluvian.voyage.ui.components.button.footer.UpvoteButton
import com.dluvian.voyage.ui.theme.OPBlue
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun UIEventActions(
    uiEvent: UIEvent,
    onUpdate: (Cmd) -> Unit,
    additionalStartAction: @Composable () -> Unit = {},
    additionalEndAction: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            additionalStartAction()
        }
        Spacer(modifier = Modifier.width(spacing.tiny))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiEvent.bookmarked) {
                BookmarkIconButton(uiEvent.event.id(), onUpdate)
                Spacer(modifier = Modifier.width(spacing.large))
            }

            CrossPostIconButton(uiEvent.eventInnerFirst(), onUpdate)
            Spacer(modifier = Modifier.width(spacing.large))

            additionalEndAction()
            Spacer(modifier = Modifier.width(spacing.large))
            OPBlue
            UpvoteButton(uiEvent = uiEvent, onUpdate = onUpdate)
        }
    }
}
