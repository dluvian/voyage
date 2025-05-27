package com.dluvian.voyage

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource


@Composable
fun getTransparentTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )
}

@Composable
@Stable
fun getListTabHeaders(numOfProfiles: Int, numOfTopics: Int): List<String> {
    val profileHeader = stringResource(id = R.string.profiles)
    val topicHeader = stringResource(id = R.string.topics)
    return remember(numOfProfiles, numOfTopics) {
        listOf(
            profileHeader + if (numOfProfiles > 0) " ($numOfProfiles)" else "",
            topicHeader + if (numOfTopics > 0) " ($numOfTopics)" else "",
        )
    }
}

@Composable
fun canAddAnotherTopic(selectedItemLength: Int, maxItems: Int = MAX_TOPICS): Boolean {
    return remember(selectedItemLength) { maxItems - selectedItemLength > 1 }
}

@Composable
fun LazyListState.showScrollButton(): Boolean {
    val hasOffset by remember { derivedStateOf { this.firstVisibleItemIndex > 2 } }
    var hasScrolled by remember(this) { mutableStateOf(false) }
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                hasScrolled = true
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value && hasScrolled && hasOffset
}
