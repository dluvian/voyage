package com.dluvian.voyage.ui.components.row

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.R
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.ui.components.FullHorizontalDivider
import com.dluvian.voyage.ui.components.button.RemoveIconButton
import com.dluvian.voyage.ui.components.text.TextInput
import com.dluvian.voyage.ui.theme.AddCircleIcon
import com.dluvian.voyage.ui.theme.OptionItemIcon
import com.dluvian.voyage.ui.theme.VotedIcon
import com.dluvian.voyage.ui.theme.sizing
import com.dluvian.voyage.ui.theme.spacing

@Composable
fun PollOptionRow(
    label: String,
    isSelected: Boolean,
    isRevealed: Boolean,
    percentage: Int,
    progress: Float,
    onClick: Fn,
) {
    val isRevealedSelection = remember(isSelected, isRevealed) {
        isSelected && isRevealed
    }
    Column {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.width(42.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRevealed) Text(
                    modifier = Modifier.padding(vertical = spacing.xl),
                    text = "$percentage%",
                    fontWeight = if (isRevealedSelection) FontWeight.SemiBold else null
                )
                else RadioButton(selected = isSelected, onClick = onClick)
            }
            Spacer(modifier = Modifier.padding(start = spacing.small))
            if (isRevealedSelection) {
                Icon(
                    modifier = Modifier.size(sizing.smallIndicator),
                    imageVector = VotedIcon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(start = spacing.small))

            }
            Text(text = label, fontWeight = if (isRevealedSelection) FontWeight.SemiBold else null)
        }
        Row {
            Spacer(modifier = Modifier.width(spacing.medium))
            if (isRevealed) LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progress })
            else FullHorizontalDivider()
        }
    }
}

@Composable
fun PollOptionInputRow(input: MutableState<TextFieldValue>, onRemove: Fn) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(
                imageVector = OptionItemIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        TextInput(
            value = input.value,
            onValueChange = { str -> input.value = str },
            placeholder = stringResource(R.string.option),
            maxLines = 2,
            trailingIcon = {
                RemoveIconButton(
                    onRemove = onRemove,
                    description = stringResource(R.string.remove_poll_option)
                )
            }
        )
    }
}

@Composable
fun PollOptionAddRow(onClick: Fn) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = AddCircleIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            modifier = Modifier.padding(start = spacing.medium),
            text = stringResource(R.string.add_an_option),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
