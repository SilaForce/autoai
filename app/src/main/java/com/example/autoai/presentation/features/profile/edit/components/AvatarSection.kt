package com.example.autoai.presentation.features.profile.edit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.VerdantGreen
import com.example.autoai.presentation.util.ImageUtils


@Composable
fun AvatarSection(
    userInitial: String,
    profilePictureUrl: String?,
    selectedProfilePicture: ByteArray?,
    onAvatarClick: () -> Unit,
) {
    // Ako imamo lokalnu sliku, prikazujemo nju. Ako ne, prikazujemo onaj URL s interneta.
    val imageModel: Any? = when {
        selectedProfilePicture != null -> selectedProfilePicture
        !profilePictureUrl.isNullOrBlank() -> ImageUtils.decodeBase64ToByteArray(profilePictureUrl)
        else -> null
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clickable(onClick = onAvatarClick),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .background(color = VerdantGreen, shape = CircleShape)
                    .clip(CircleShape),
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = AppStrings.Profile.avatarDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = userInitial,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                    )
                }
            }

            // Camera badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .background(color = VerdantGreen, shape = CircleShape)
                    .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = AppStrings.EditProfile.changeAvatar,
            fontSize = 13.sp,
            color = VerdantGreen,
            fontWeight = FontWeight.Medium,
        )
    }
}
