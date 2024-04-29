package com.dluvian.voyage.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.ThumbDownAlt
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.model.WebTrust

val HomeIcon = Icons.Default.Home
val DiscoverIcon = Icons.Default.TravelExplore
val AddIcon = Icons.Default.Add
val RemoveCircleIcon = Icons.Default.RemoveCircle
val InboxIcon = Icons.Default.Notifications
val SettingsIcon = Icons.Default.Settings
val BackIcon = Icons.AutoMirrored.Filled.ArrowBack
val CommentIcon = Icons.AutoMirrored.Filled.Comment
val AccountIcon = Icons.Default.AccountCircle
val HashtagIcon = Icons.Default.Tag
val UpvoteOffIcon = Icons.Default.ThumbUpOffAlt
val DownvoteOffIcon = Icons.Default.ThumbDownOffAlt
val UpvoteIcon = Icons.Default.ThumbUpAlt
val DownvoteIcon = Icons.Default.ThumbDownAlt
val SearchIcon = Icons.Default.Search
val SendIcon = Icons.AutoMirrored.Filled.Send
val ReplyIcon = Icons.AutoMirrored.Filled.Reply
val ScrollUpIcon = Icons.Default.KeyboardDoubleArrowUp
val SaveIcon = Icons.Default.Save
val ExpandIcon = Icons.Default.ExpandMore
val CollapseIcon = Icons.Default.ExpandLess
val HorizMoreIcon = Icons.Default.MoreHoriz
val DeleteIcon = Icons.Default.Delete
val CrossPostIcon = Icons.Default.Repeat

@Stable
@Composable
fun getTrustIcon(trustType: TrustType): ImageVector {
    return when (trustType) {
        Oneself -> Icons.Default.Star
        FriendTrust, WebTrust -> Icons.Filled.VerifiedUser
        NoTrust -> Icons.Default.QuestionMark
    }
}
