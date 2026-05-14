package com.example.autoai.presentation.features.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.BottomNavigationBar
import com.example.autoai.presentation.features.chat.components.ChatMessageItem
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.VerdantGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatContent(
    state: AiChatState,
    snackbarHostState: SnackbarHostState,
    onEvent: (AiChatEvent) -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) onEvent(AiChatEvent.OnImageSelected(bytes))
            }
        }
    )

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = state.selectedNavItem,
                    onItemSelected = { onEvent(AiChatEvent.OnNavItemSelected(it)) }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Lista poruka
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    // ── Header ────────────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = AppStrings.Chat.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    items(state.messages) { message ->
                        ChatMessageItem(message = message)
                    }

                    if (state.isAiTyping) {
                        item {
                            Text(
                                text = "AI kuca...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 40.dp, top = 8.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // ── Image preview ─────────────────────────────
                        if (state.selectedImage != null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            ) {
                                AsyncImage(
                                    model = state.selectedImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onEvent(AiChatEvent.OnClearImageClicked) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear image",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = "Pick image",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            OutlinedTextField(
                                value = state.inputText,
                                onValueChange = { onEvent(AiChatEvent.OnInputChanged(it)) },
                                placeholder = {
                                    Text(
                                        AppStrings.Chat.placeholder,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = VerdantGreen,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onEvent(AiChatEvent.OnSendMessageClicked) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    ),
                                enabled = state.inputText.isNotBlank() && !state.isAiTyping
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = AppStrings.Chat.sendDescription,
                                    tint = if (state.inputText.isNotBlank()) VerdantGreen else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.3f
                                    )
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiChatContentPreview() {
    AutoAITheme {
        AiChatContent(
            state = AiChatState(
                messages = listOf(
                    ChatMessageUi(id = "1", text = "Zdravo! Kako mogu da vam pomognem?", isFromUser = false, formattedTime = "15:16"),
                    ChatMessageUi(id = "2", text = "Koje su opcije finansiranja dostupne?", isFromUser = true, formattedTime = "15:16"),
                    ChatMessageUi(id = "3", text = "Imamo nekoliko opcija, uključujući leasing i kredit. Koja vas zanima?", isFromUser = false, formattedTime = "15:17"),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
        )
    }
}
