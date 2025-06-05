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
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.ui.components.button.footer.BookmarkIconButton
import com.dluvian.voyage.ui.components.button.footer.CrossPostIconButton
import com.dluvian.voyage.ui.components.button.footer.UpvoteButton
import com.dluvian.voyage.ui.theme.OPBlue
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun UIEventActions(
    uiEvent: MainEvent,
    onUpdate: OnUpdate,
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
            if (uiEvent.isBookmarked) {
                BookmarkIconButton(relevantId = uiEvent.getRelevantId(), onUpdate = onUpdate)
                Spacer(modifier = Modifier.width(spacing.large))
            }
            when (uiEvent) {
                is CrossPost,
                is RootPost,
                is Comment,
                is LegacyReply -> {
                    CrossPostIconButton(relevantId = uiEvent.getRelevantId(), onUpdate = onUpdate)
                    Spacer(modifier = Modifier.width(spacing.large))
                }
            }
            additionalEndAction()
            Spacer(modifier = Modifier.width(spacing.large))
            OPBlue
            UpvoteButton(mainEvent = uiEvent, onUpdate = onUpdate)
        }
    }
}
