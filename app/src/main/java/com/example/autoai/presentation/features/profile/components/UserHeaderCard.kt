package com.example.autoai.presentation.features.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.SubtleBorder
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.autoai.presentation.util.ImageUtils

@Composable
fun UserHeaderCard(
    userInitial: String,
    fullName: String,
    username: String,
    userEmail: String,
    phoneNumber: String,
    profilePictureUrl: String?,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, SubtleBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // ── TOP SEKCIJA: Avatar + Ime + Username ─────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .background(color = VerdantGreen, shape = CircleShape)
                        .clip(CircleShape),
                ) {
                    if (!profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageUtils.decodeBase64ToByteArray(profilePictureUrl),
                            contentDescription = "Profile Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = userInitial,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Ime i Username
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = fullName.ifBlank { "Korisnik" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val displayUsername = if (username.isNotBlank()) "@$username" else "@korisnik"
                    Text(
                        text = displayUsername,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = VerdantGreen.copy(alpha = 0.8f), // Zelenkasta za @username daje lijep akcenat
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── SREDNJA SEKCIJA: Kontakt info sa ikonicama ───────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Email Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = CharcoalGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = CharcoalGray.copy(alpha = 0.7f),
                    )
                }

                // Phone Row (prikazuje se samo ako nije prazan)
                if (phoneNumber.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = CharcoalGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = phoneNumber,
                            fontSize = 14.sp,
                            color = CharcoalGray.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── BOTTOM SEKCIJA: Dugme na punu širinu ────────────────────
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Text(
                    text = AppStrings.Profile.editButton,
                    color = CharcoalGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}