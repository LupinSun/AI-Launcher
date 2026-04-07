package de.mm20.launcher2.ui.launcher.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.launcher.assistant.AssistantContent

internal object AssistantComponent : ScaffoldComponent() {
    override val isAtTop: MutableState<Boolean?> = mutableStateOf(true)
    override val isAtBottom: MutableState<Boolean?> = mutableStateOf(true)
    override val hasIme: Boolean = true
    override val permanent: Boolean = false

    @Composable
    override fun Component(
        modifier: Modifier,
        insets: PaddingValues,
        state: LauncherScaffoldState,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(insets)
                .navigationBarsPadding()
                .imePadding()
        ) {
            AssistantContent(modifier = Modifier.fillMaxWidth().weight(1f))
        }
    }
}
