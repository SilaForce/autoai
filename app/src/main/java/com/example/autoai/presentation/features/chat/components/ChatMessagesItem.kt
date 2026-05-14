package com.example.autoai.presentation.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.presentation.features.chat.ChatMessageUi
import androidx.compose.material3.MaterialTheme
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.VerdantGreen

@Composable
fun ChatMessageItem(message: ChatMessageUi) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // AI Ikona (možeš zamijeniti svojim drawable resursom ako imaš specifičnu ikonu robota)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 16.sp) // Privremeni emoji dok ne ubaciš ikonu
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = if (isUser) {
                RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
            } else {
                RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) VerdantGreen else MaterialTheme.colorScheme.surface,
                contentColor = if (isUser) PureWhite else MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.formattedTime,
                    fontSize = 10.sp,
                    color = if (isUser) PureWhite.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}