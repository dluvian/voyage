package com.dluvian.voyage.core.model.filter

sealed class EvalFilter
data object Latest : EvalFilter()
// TODO: data object Top : EvalFilter()
// TODO: data object Controversial : EvalFilter()
