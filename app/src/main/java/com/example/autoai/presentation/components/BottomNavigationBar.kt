package com.example.autoai.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.R
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.VerdantGreen

enum class BottomNavItem(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(R.string.home_nav_home, Icons.Outlined.Home),
    GARAGE(R.string.home_nav_garage, Icons.Outlined.DirectionsCar),
    COSTS(R.string.home_nav_costs, Icons.Outlined.RequestQuote),
    REMINDERS(R.string.home_nav_reminders, Icons.Outlined.Notifications),
    AI_CHAT(R.string.home_nav_ai_chat, Icons.Outlined.ChatBubbleOutline),
}

@Composable
fun BottomNavigationBar(
    selectedItem: BottomNavItem = BottomNavItem.HOME,
    onItemSelected: (BottomNavItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = PureWhite,
        tonalElevation = 0.dp,
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = item == selectedItem
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VerdantGreen,
                    selectedTextColor = VerdantGreen,
                    indicatorColor = Color(0xFFE8F5E9),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                )
            )
        }
    }
}

