package com.dreamyloong.tlauncher.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PageScaffold(
    title: String,
    subtitle: String,
    contentScrollState: ScrollState,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    AnimatedContent(
                        targetState = title,
                        transitionSpec = {
                            (
                                fadeIn(
                                    animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                                ) + slideInVertically(
                                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                                    initialOffsetY = { it / 8 },
                                )
                            ).togetherWith(
                                fadeOut(
                                    animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing),
                                ) + slideOutVertically(
                                    animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                                    targetOffsetY = { -it / 10 },
                                ),
                            )
                        },
                        label = "page_title_transition",
                    ) { animatedTitle ->
                        Text(
                            text = animatedTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    AnimatedContent(
                        targetState = subtitle,
                        transitionSpec = {
                            (
                                fadeIn(
                                    animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                                ) + slideInVertically(
                                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                                    initialOffsetY = { it / 8 },
                                )
                            ).togetherWith(
                                fadeOut(
                                    animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing),
                                ),
                            )
                        },
                        label = "page_subtitle_transition",
                    ) { animatedSubtitle ->
                        Text(
                            text = animatedSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AnimatedVisibility(
                    visible = actionLabel != null && onActionClick != null,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                    ) + expandHorizontally(
                        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.End,
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing),
                    ) + shrinkHorizontally(
                        animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.End,
                    ),
                ) {
                    val resolvedActionLabel = actionLabel
                    val resolvedActionClick = onActionClick
                    if (resolvedActionLabel != null && resolvedActionClick != null) {
                        FilledTonalButton(onClick = resolvedActionClick) {
                            Text(resolvedActionLabel)
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(contentScrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
                footerContent?.let { footer ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        footer()
                    }
                }
        },
    )
}
