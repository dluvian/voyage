package com.dluvian.voyage.ui.components.row.mainEvent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.Comment
import com.dluvian.voyage.core.model.CrossPost
import com.dluvian.voyage.core.model.LegacyReply
import com.dluvian.voyage.core.model.MainEvent
import com.dluvian.voyage.core.model.Poll
import com.dluvian.voyage.core.model.RootPost
import com.dluvian.voyage.ui.components.button.footer.BookmarkIconButton
import com.dluvian.voyage.ui.components.button.footer.CountedUpvoteButton
import com.dluvian.voyage.ui.components.button.footer.CrossPostIconButton
import com.dluvian.voyage.ui.theme.OPBlue
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun MainEventActions(
    mainEvent: MainEvent,
    onUpdate: OnUpdate,
    additionalStartAction: ComposableContent = {},
    additionalEndAction: ComposableContent = {},
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
            if (mainEvent.isBookmarked) {
                BookmarkIconButton(relevantId = mainEvent.getRelevantId(), onUpdate = onUpdate)
                Spacer(modifier = Modifier.width(spacing.large))
            }
            when (mainEvent) {
                is Poll,
                is CrossPost,
                is RootPost,
                is Comment,
                is LegacyReply -> {
                    CrossPostIconButton(relevantId = mainEvent.getRelevantId(), onUpdate = onUpdate)
                    Spacer(modifier = Modifier.width(spacing.large))
                }
            }
            additionalEndAction()
            Spacer(modifier = Modifier.width(spacing.large))
            OPBlue
            CountedUpvoteButton(mainEvent = mainEvent, onUpdate = onUpdate)
        }
    }
}
