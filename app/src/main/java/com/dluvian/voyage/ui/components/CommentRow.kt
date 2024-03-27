package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.model.CommentUI
import com.dluvian.voyage.ui.theme.ReplyIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun CommentRow(comment: CommentUI, onUpdate: OnUpdate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.screenEdge)
    ) {
        PostRowHeader(
            trustType = comment.trustType,
            authorName = comment.authorName,
            pubkey = comment.pubkey,
            isDetailed = true,
            createdAt = comment.createdAt,
            myTopic = null,
            collapsedText = if (comment.isCollapsed) comment.content else null,
            onUpdate = onUpdate
        )
        if (!comment.isCollapsed) {
            Spacer(modifier = Modifier.height(spacing.large))
            Text(
                text = comment.content,
                maxLines = Int.MAX_VALUE,
            )
            Spacer(modifier = Modifier.height(spacing.large))
            PostRowActions(
                id = comment.id,
                pubkey = comment.pubkey,
                myVote = comment.myVote,
                tally = comment.tally,
                onUpdate = onUpdate,
                additionalAction = {
                    TextButton(
                        modifier = Modifier.height(ButtonDefaults.MinHeight),
                        onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = ReplyIcon,
                            contentDescription = stringResource(id = R.string.reply)
                        )
                        Text(text = stringResource(id = R.string.reply))
                    }
                }
            )
        }
    }
}
