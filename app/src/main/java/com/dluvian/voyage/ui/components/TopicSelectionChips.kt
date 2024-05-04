package com.dluvian.voyage.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.MAX_TOPICS
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.ui.components.chip.TopicChip
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun TopicSelectionChips(
    modifier: Modifier = Modifier,
    myTopics: List<Topic>,
    selectedTopics: MutableState<List<Topic>>,
) {
    if (myTopics.isEmpty()) {
        Text(
            modifier = Modifier.padding(bottom = spacing.xxl),
            text = stringResource(id = R.string.you_dont_follow_any_topics_yet),
            style = MaterialTheme.typography.titleMedium
        )
    } else {
        val isEnabled =
            remember(selectedTopics.value.size) { selectedTopics.value.size < MAX_TOPICS }
        LazyVerticalStaggeredGrid(
            modifier = modifier,
            columns = StaggeredGridCells.Adaptive(minSize = sizing.topicChipMinSize),
            verticalItemSpacing = spacing.small,
            contentPadding = PaddingValues(bottom = spacing.xxl)
        ) {
            items(myTopics) { topic ->
                val isSelected = selectedTopics.value.contains(topic)
                TopicChip(
                    modifier = Modifier.padding(spacing.medium),
                    isSelected = isSelected,
                    isEnabled = isEnabled || isSelected,
                    heightRatio = 0.85f,
                    topic = topic,
                    onClick = {
                        if (isSelected) selectedTopics.value -= topic
                        else if (selectedTopics.value.size < MAX_TOPICS) selectedTopics.value += topic
                    }
                )
            }
        }
    }
}
