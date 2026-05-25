package com.example.autoai.presentation.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoai.localization.AppStrings
import com.example.autoai.presentation.components.MainButton
import com.example.autoai.presentation.features.onboarding.components.PageIndicator
import com.example.autoai.presentation.features.onboarding.components.OnboardingPage
import com.example.autoai.presentation.features.onboarding.components.OnboardingPageContent
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.launch


@Composable
fun OnboardingContent(
    onEvent: (OnboardingEvent) -> Unit
) {
    val onboardingPages = listOf(
        OnboardingPage(
            icon = Icons.Outlined.DirectionsCar,
            title = AppStrings.Onboarding.welcomeTitle,
            description = AppStrings.Onboarding.welcomeSubtitle
        ),
        OnboardingPage(
            icon = Icons.Outlined.RequestQuote,
            title = AppStrings.Onboarding.expensesTitle,
            description = AppStrings.Onboarding.expensesSubtitle
        ),
        OnboardingPage(
            icon = Icons.Outlined.ChatBubbleOutline,
            title = AppStrings.Onboarding.aiTitle,
            description = AppStrings.Onboarding.aiSubtitle
        )
    )

    val PAGE_COUNT = onboardingPages.size
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // ── Top bar: SKIP ────────────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(onClick = { onEvent(OnboardingEvent.OnSkipClicked) }) {
                Text(
                    text = AppStrings.Onboarding.skip,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }

        // ── Pager ─────────────────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPageContent(page = onboardingPages[pageIndex])
        }

        // ── Bottom: indicators + button ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indikator stranica
            PageIndicator(
                pageSize = PAGE_COUNT,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            val isLastPage = pagerState.currentPage == PAGE_COUNT - 1

            MainButton(
                text = if (isLastPage) AppStrings.Onboarding.getStarted else AppStrings.Onboarding.next,
                trailingIcon = if (isLastPage) null else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                onClick = {
                    if (isLastPage) {
                        onEvent(OnboardingEvent.OnGetStartedClicked)
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OnboardingContentPreview() {
    OnboardingContent(
        onEvent = {}
    )
}