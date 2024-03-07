package com.dluvian.voyage.core

sealed class UIEvent

data object BackPress : UIEvent()
data object ClickHome : UIEvent()
data object ClickTopics : UIEvent()
data object ClickCreate : UIEvent()
data object ClickInbox : UIEvent()
data object ClickSettings : UIEvent()
