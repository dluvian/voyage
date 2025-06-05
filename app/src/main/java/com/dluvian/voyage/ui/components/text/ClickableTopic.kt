package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.Cmd
import com.dluvian.voyage.model.OpenTopic

@Composable
fun ClickableTopic(topic: Topic, onUpdate: (Cmd) -> Unit, onClickAddition: () -> Unit = {}) {
    Text(
        modifier = Modifier.clickable {
            onUpdate(OpenTopic(topic = topic))
            onClickAddition()
        },
        text = "#$topic"
    )
}
