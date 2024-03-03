package com.dluvian.voyage.core

sealed class UIEvent

data object ClickPost : UIEvent()
data object BackPress : UIEvent()
data object Refresh : UIEvent()
