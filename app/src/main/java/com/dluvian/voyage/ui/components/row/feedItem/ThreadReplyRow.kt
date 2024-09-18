package com.dluvian.voyage.ui.components.row.feedItem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.ui.theme.spacing
import com.dluvian.voyage.ui.views.nonMain.MoreRepliesTextButton

@Composable
fun ThreadReplyRow(
    leveledReply: LeveledReplyUI,
    isLocalRoot: Boolean,
    isOp: Boolean,
    isCollapsed: Boolean = leveledReply.isCollapsed,
    onUpdate: OnUpdate,
) {
    RowWithDivider(level = leveledReply.level) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BaseReplyRow(
                reply = leveledReply.reply,
                isCollapsed = isCollapsed,
                showFullReplyButton = leveledReply.level == 0,
                isOp = isOp,
                isThread = true,
                onUpdate = onUpdate,
                onToggleCollapse = {
                    if (!isLocalRoot) onUpdate(
                        ThreadViewToggleCollapse(id = leveledReply.reply.id)
                    )
                },
                showAuthorName = true,
                additionalStartAction = {
                    if (leveledReply.reply.replyCount > 0 && !leveledReply.hasLoadedReplies) {
                        MoreRepliesTextButton(
                            replyCount = leveledReply.reply.replyCount,
                            onShowReplies = {
                                onUpdate(ThreadViewShowReplies(id = leveledReply.reply.id))
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun RowWithDivider(level: Int, content: ComposableContent) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        repeat(times = level) {
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = spacing.large, end = spacing.medium)
            )
        }
        content()
    }
}
