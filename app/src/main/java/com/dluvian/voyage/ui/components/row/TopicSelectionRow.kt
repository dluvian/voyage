package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.ui.components.bottomSheet.TopicSelectionBottomSheet
import com.dluvian.voyage.ui.components.chip.SmallFilterChip
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.theme.AddIcon
import com.dluvian.voyage.ui.theme.RemoveCircleIcon
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun TopicSelectionRow(topics: MutableState<List<Topic>>, myTopics: List<Topic>) {
    val showTopicSelection = remember { mutableStateOf(false) }
    if (showTopicSelection.value) TopicSelectionBottomSheet(
        selectedTopics = topics,
        myTopics = myTopics,
        onDismiss = { showTopicSelection.value = false })
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.bigScreenEdge)
    ) {
        if (topics.value.size < MAX_TOPICS) item {
            AddTopicChip(onOpenTopicSelection = { showTopicSelection.value = true })
        }
        items(topics.value) {
            TopicChip(
                modifier = Modifier.padding(horizontal = spacing.small),
                topic = it,
                trailingImageVector = RemoveCircleIcon,
                onClick = { topics.value -= it })
        }
    }
}

@Composable
private fun AddTopicChip(onOpenTopicSelection: Fn) {
    SmallFilterChip(
        onClick = onOpenTopicSelection,
        label = { Text(text = stringResource(id = R.string.topics)) },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                imageVector = AddIcon,
                contentDescription = stringResource(id = R.string.topics),
            )
        },
    )
}
