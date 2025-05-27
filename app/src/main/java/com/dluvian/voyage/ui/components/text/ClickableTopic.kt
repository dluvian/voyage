package com.dluvian.voyage.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.voyage.OpenTopic
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.OnUpdate
import com.dluvian.voyage.core.Topic

@Composable
fun ClickableTopic(topic: Topic, onUpdate: OnUpdate, onClickAddition: Fn = {}) {
    Text(
        modifier = Modifier.clickable {
            onUpdate(OpenTopic(topic = topic))
            onClickAddition()
        },
        text = "#$topic"
    )
}
