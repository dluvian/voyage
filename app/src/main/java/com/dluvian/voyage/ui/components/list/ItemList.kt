package com.dluvian.voyage.ui.components.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.voyage.R
import com.dluvian.voyage.core.ComposableContent
import com.dluvian.voyage.ui.components.button.RemoveIconButton
import com.dluvian.voyage.ui.components.indicator.BaseHint
import com.dluvian.voyage.ui.components.row.ClickableRow


typealias ItemProps = Pair<ComposableContent, String>

@Composable
fun ItemList(
    items: List<ItemProps>,
    state: LazyListState,
    isRemovable: Boolean,
    firstRow: ComposableContent = {},
    onRemove: (Int) -> Unit = {},
) {
    if (!isRemovable && items.isEmpty()) {
        BaseHint(text = stringResource(id = R.string.no_items_found))
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
        item {
            firstRow()
        }
        itemsIndexed(items) { i, (icon, label) ->
            ClickableRow(
                header = label,
                leadingContent = icon,
                trailingContent = {
                    if (isRemovable) {
                        RemoveIconButton(onRemove = { onRemove(i) })
                    }
                },
                onClick = { }
            )
        }
    }
}
