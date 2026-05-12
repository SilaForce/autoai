package com.example.autoai.presentation.features.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.SubtleBorder

@Composable
fun AccountInfoCard(
    memberSince: String,
    plan: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = AppStrings.Profile.accountInfoTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalGray,
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = AppStrings.Profile.memberSinceLabel, value = memberSince)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = SubtleBorder,
            )

            InfoRow(label = AppStrings.Profile.planLabel, value = plan)
        }
    }
}