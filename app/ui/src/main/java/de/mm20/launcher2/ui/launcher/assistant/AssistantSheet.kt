package de.mm20.launcher2.ui.launcher.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.ai.AiMessage
import de.mm20.launcher2.aiassistant.AssistantState
import de.mm20.launcher2.ui.component.DismissableBottomSheet

@Composable
fun AssistantSheet(
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    DismissableBottomSheet(expanded = expanded, onDismissRequest = onDismiss) {
        val viewModel: AssistantSheetVM = viewModel()
        val messages by viewModel.messages.collectAsState()
        val state by viewModel.state.collectAsState()

        val listState = rememberLazyListState()
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(
                    painter = painterResource(de.mm20.launcher2.base.R.drawable.auto_awesome_24dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Assistant",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(horizontal = 8.dp),
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
                if (state is AssistantState.Streaming) {
                    item {
                        StreamingBubble(text = (state as AssistantState.Streaming).partialText)
                    }
                }
                if (state is AssistantState.Thinking) {
                    item {
                        Box(modifier = Modifier.padding(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                }
                if (state is AssistantState.Error) {
                    item {
                        Text(
                            text = "Error: ${(state as AssistantState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }

            // Input bar
            AssistantInputBar(
                enabled = state is AssistantState.Idle || state is AssistantState.Error,
                onSend = { text -> viewModel.sendMessage(text) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun ChatBubble(message: AiMessage) {
    val isUser = message.role == AiMessage.Role.User
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser)
            androidx.compose.foundation.layout.Arrangement.End
        else
            androidx.compose.foundation.layout.Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp,
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StreamingBubble(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = 4.dp, bottomEnd = 16.dp,
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AssistantInputBar(
    enabled: Boolean,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask anything…") },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }),
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            },
            enabled = enabled && text.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "Send",
            )
        }
    }
}
