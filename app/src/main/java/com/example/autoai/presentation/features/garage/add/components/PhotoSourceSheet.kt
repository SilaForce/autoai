package com.example.autoai.presentation.features.garage.add.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onGallerySelected: () -> Unit,
    onCameraSelected: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            PhotoSourceRow(
                icon = Icons.Outlined.PhotoLibrary,
                label = AppStrings.AddVehicle.photoSourceGallery,
                onClick = onGallerySelected,
            )
            PhotoSourceRow(
                icon = Icons.Outlined.CameraAlt,
                label = AppStrings.AddVehicle.photoSourceCamera,
                onClick = onCameraSelected,
            )
        }
    }
}

@Composable
private fun PhotoSourceRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
