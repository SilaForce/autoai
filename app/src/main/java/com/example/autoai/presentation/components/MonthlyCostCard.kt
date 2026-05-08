package com.example.autoai.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.presentation.theme.CharcoalGray
import com.example.autoai.presentation.theme.PureWhite
import com.example.autoai.presentation.theme.VerdantGreen

@Composable
fun MonthlyCostCard(
    label: String,
    amount: String,
    currency: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = CharcoalGray.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = amount,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdantGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currency,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalGray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun MonthlyCostCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        MonthlyCostCard(label = "Troškovi ovog mjeseca", amount = "1,045", currency = "KM")
    }
}

