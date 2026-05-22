package com.example.autoai.presentation.features.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.autoai.R
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.BottomNavigationBar
import com.example.autoai.presentation.features.chat.components.ChatMessageItem
import com.example.autoai.presentation.features.chat.components.ChatSidebarContent
import com.example.autoai.presentation.theme.AutoAITheme
import com.example.autoai.presentation.theme.VerdantGreen
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiChatContent(
    state: AiChatState,
    snackbarHostState: SnackbarHostState,
    onEvent: (AiChatEvent) -> Unit
) {
    val listState = rememberLazyListState()
    val imeVisible = WindowInsets.isImeVisible
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

    // ── Camera capture ────────────────────────────────────────────
    val cameraImageUri = remember {
        val cameraDir = File(context.cacheDir, "camera_images").apply { mkdirs() }
        val imageFile = File(cameraDir, "chat_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val bytes = context.contentResolver.openInputStream(cameraImageUri)?.use { it.readBytes() }
                if (bytes != null) onEvent(AiChatEvent.OnImageSelected(bytes))
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                cameraLauncher.launch(cameraImageUri)
            }
        }
    )

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(cameraImageUri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ChatSidebarContent(
                    threads = state.threads,
                    currentThreadId = state.currentThreadId,
                    threadMenuAnchorId = state.threadMenuAnchorId,
                    onStartNewChat = {
                        onEvent(AiChatEvent.OnStartNewChat)
                        drawerScope.launch { drawerState.close() }
                    },
                    onSelectThread = { id ->
                        onEvent(AiChatEvent.OnSelectThread(id))
                        drawerScope.launch { drawerState.close() }
                    },
                    onLongPressThread = { id -> onEvent(AiChatEvent.OnLongPressThread(id)) },
                    onDismissThreadMenu = { onEvent(AiChatEvent.OnDismissThreadMenu) },
                    onDeleteThread = { id -> onEvent(AiChatEvent.OnDeleteThreadClicked(id)) },
                )
            }
        },
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = state.currentThreadTitle
                                ?: stringResource(R.string.chat_default_title),
                            maxLines = 1,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { drawerScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.chat_drawer_open_history),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            bottomBar = {
                AnimatedVisibility(visible = !imeVisible) {
                    BottomNavigationBar(
                        selectedItem = state.selectedNavItem,
                        onItemSelected = { onEvent(AiChatEvent.OnNavItemSelected(it)) }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (state.messages.isEmpty() && !state.isAiTyping) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = AppStrings.Chat.emptyStateTitle,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        item { Spacer(modifier = Modifier.height(12.dp)) }

                        items(state.messages, key = { it.id }) { message ->
                            ChatMessageItem(message = message)
                        }

                        if (state.isAiTyping) {
                            item {
                                Text(
                                    text = "AI...",
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
                }

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // ── Image previews ────────────────────────────
                        if (state.selectedImages.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.selectedImages.forEachIndexed { index, imageBytes ->
                                    Box {
                                        AsyncImage(
                                            model = imageBytes,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { onEvent(AiChatEvent.OnRemoveImage(index)) },
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
                                                contentDescription = "Remove image",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery button
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

                            // Camera button
                            IconButton(onClick = { launchCamera() }) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = "Take photo",
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

        if (state.pendingDeleteThreadId != null) {
            AlertDialog(
                onDismissRequest = { onEvent(AiChatEvent.OnDismissDeleteDialog) },
                title = { Text(stringResource(R.string.chat_delete_dialog_title)) },
                text = { Text(stringResource(R.string.chat_delete_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = { onEvent(AiChatEvent.OnConfirmDeleteThread) }) {
                        Text(
                            text = stringResource(R.string.chat_delete_confirm),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(AiChatEvent.OnDismissDeleteDialog) }) {
                        Text(stringResource(R.string.chat_delete_cancel))
                    }
                },
            )
        }
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
