package com.example.autoai.presentation.features.garage.add.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun VehiclePhotoSection(
    existingPhotoBase64: String?,
    selectedPhotoBytes: ByteArray?,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    // Decode the saved photo once per change (form recomposes on every keystroke, so
    // re-running Base64.decode every time would be wasteful for a ~30KB string).
    val decodedExisting = remember(existingPhotoBase64) {
        existingPhotoBase64?.takeIf { it.isNotBlank() }
            ?.let { ImageUtils.decodeBase64ToByteArray(it) }
    }
    // A freshly picked photo wins over the saved one (matches AvatarSection precedence).
    val imageModel: Any? = selectedPhotoBytes ?: decodedExisting
    val hasPhoto = imageModel != null

    Text(
        text = AppStrings.AddVehicle.photoLabel,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = VerdantGreen,
                    modifier = Modifier.size(40.dp),
                )
            }

            // Camera-overlay badge — same visual affordance the avatar uses, so users see
            // the slot is tappable without needing an explicit "Add photo" button.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .background(VerdantGreen, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (hasPhoto) AppStrings.AddVehicle.photoChange else AppStrings.AddVehicle.photoAdd,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = VerdantGreen,
                modifier = Modifier.clickable(onClick = onClick),
            )
            if (hasPhoto) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onRemoveClick,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                ) {
                    Text(
                        text = AppStrings.AddVehicle.photoRemove,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
