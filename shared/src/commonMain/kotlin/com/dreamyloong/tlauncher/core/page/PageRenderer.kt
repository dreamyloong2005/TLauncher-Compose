package com.dreamyloong.tlauncher.core.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dreamyloong.tlauncher.core.platform.BindTextInputDismissHandler
import com.dreamyloong.tlauncher.core.platform.SystemBackCoordinator
import com.dreamyloong.tlauncher.ui.component.PageScaffold
import com.dreamyloong.tlauncher.ui.component.SectionTitle
import com.dreamyloong.tlauncher.ui.component.SummaryCard
import com.dreamyloong.tlauncher.ui.component.ValueRow
import kotlinx.coroutines.delay

private const val MotionShort = 90
private const val MotionMedium = 150
private const val MotionLong = 190
private val LaunchBarTrailingButtonSize = 40.dp

@Composable
fun RenderLauncherPage(page: ResolvedPage) {
    val expandedCards = remember { mutableStateMapOf<String, Boolean>() }
    val contentScrollState = rememberSaveable(page.id, saver = ScrollState.Saver) {
        ScrollState(initial = 0)
    }
    val autoRefresh = remember(page) { page.firstAutoRefreshWidget() }
    LaunchedEffect(page.id) {
        expandedCards.clear()
    }
    LaunchedEffect(page.id, autoRefresh?.intervalMillis) {
        val refreshWidget = autoRefresh ?: return@LaunchedEffect
        while (true) {
            delay(refreshWidget.intervalMillis)
            refreshWidget.onRefresh()
        }
    }
    PageScaffold(
        title = page.title,
        subtitle = page.subtitle.orEmpty(),
        contentScrollState = contentScrollState,
        actionLabel = page.actionLabel,
        onActionClick = page.action,
        footerContent = page.footerNodes.takeIf { it.isNotEmpty() }?.let { footerNodes ->
            {
                footerNodes.forEachIndexed { index, node ->
                    val footerLayout = node.footerLayout ?: PageFooterLayoutRegistration()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = footerLayout.horizontalPaddingDp.dp,
                                end = footerLayout.horizontalPaddingDp.dp,
                                top = footerLayout.topPaddingDp.dp,
                                bottom = footerLayout.bottomPaddingDp.dp,
                            ),
                    ) {
                        AnimatedPageNode(
                            node = node,
                            depth = 0,
                            index = index,
                            expandedCards = expandedCards,
                        )
                    }
                }
            }
        },
    ) {
        page.nodes.forEachIndexed { index, node ->
            AnimatedPageNode(
                node = node,
                depth = 0,
                index = index,
                expandedCards = expandedCards,
            )
        }
    }
}

@Composable
private fun AnimatedPageNode(
    node: ResolvedPageNode,
    depth: Int,
    index: Int,
    expandedCards: MutableMap<String, Boolean>,
) {
    var visible by remember(node.nodeId) { mutableStateOf(false) }
    val delay = nodeRevealDelay(depth, index)
    LaunchedEffect(node.nodeId) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = MotionMedium,
                delayMillis = delay,
                easing = FastOutSlowInEasing,
            ),
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = MotionLong,
                delayMillis = delay,
                easing = FastOutSlowInEasing,
            ),
            expandFrom = Alignment.Top,
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = MotionShort,
                easing = FastOutSlowInEasing,
            ),
        ) + shrinkVertically(
            animationSpec = tween(
                durationMillis = MotionShort,
                easing = FastOutSlowInEasing,
            ),
            shrinkTowards = Alignment.Top,
        ),
    ) {
        RenderPageNode(
            node = node,
            depth = depth,
            expandedCards = expandedCards,
        )
    }
}

@Composable
private fun RenderPageNode(
    node: ResolvedPageNode,
    depth: Int,
    expandedCards: MutableMap<String, Boolean>,
) {
    when (node) {
        is ResolvedPageNode.Section -> {
            val sectionModifier = if (depth == 0) Modifier else Modifier.padding(start = 12.dp)
            Column(
                modifier = sectionModifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                node.title?.takeIf { it.isNotBlank() }?.let { title ->
                    SectionTitle(title)
                }
                node.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                node.children.forEachIndexed { index, child ->
                    AnimatedPageNode(
                        node = child,
                        depth = depth + 1,
                        index = index,
                        expandedCards = expandedCards,
                    )
                }
            }
        }

        is ResolvedPageNode.Widget -> {
            when (val widget = node.widget) {
                is ResolvedPageWidget.SummaryCard -> {
                    val colors = widget.tone.colors()
                    SummaryCard(
                        title = widget.title,
                        subtitle = widget.subtitle,
                        modifier = widget.onClick?.let { onClick ->
                            Modifier.clickable(onClick = onClick)
                        } ?: Modifier,
                        containerColor = colors.container,
                        titleColor = colors.title,
                        subtitleColor = colors.subtitle,
                    )
                }

                is ResolvedPageWidget.ValueCard -> ValueCard(widget.rows)
                is ResolvedPageWidget.DetailCard -> DetailCard(widget)
                is ResolvedPageWidget.ProgressCard -> ProgressCard(widget)
                is ResolvedPageWidget.ChoiceCard -> {
                    val expanded = expandedCards[node.nodeId] == true
                    ChoiceCard(
                        widget = widget,
                        expanded = expanded,
                        onToggle = {
                            expandedCards[node.nodeId] = !expanded
                        },
                    )
                }

                is ResolvedPageWidget.ToggleCard -> ToggleCard(widget)
                is ResolvedPageWidget.ButtonStack -> ButtonStack(widget.actions)
                is ResolvedPageWidget.LaunchBar -> LaunchBar(widget)
                is ResolvedPageWidget.AutoRefresh -> Unit
                is ResolvedPageWidget.TextInputCard -> TextInputCard(
                    nodeId = node.nodeId,
                    widget = widget,
                )
                is ResolvedPageWidget.DirectoryInputCard -> DirectoryInputCard(
                    nodeId = node.nodeId,
                    widget = widget,
                )
            }
        }
    }
}

private fun ResolvedPage.firstAutoRefreshWidget(): ResolvedPageWidget.AutoRefresh? {
    return footerNodes.firstNotNullOfOrNull { node ->
        (node.widget as? ResolvedPageWidget.AutoRefresh)
    } ?: nodes.firstAutoRefreshWidget()
}

private fun List<ResolvedPageNode>.firstAutoRefreshWidget(): ResolvedPageWidget.AutoRefresh? {
    for (node in this) {
        when (node) {
            is ResolvedPageNode.Section -> {
                node.children.firstAutoRefreshWidget()?.let { return it }
            }
            is ResolvedPageNode.Widget -> {
                (node.widget as? ResolvedPageWidget.AutoRefresh)?.let { return it }
            }
        }
    }
    return null
}

@Composable
private fun ValueCard(rows: List<ResolvedPageValueItem>) {
    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "value_card_container_color",
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            rows.forEach { row ->
                ValueRow(
                    label = row.label,
                    value = row.value,
                )
            }
        }
    }
}

@Composable
private fun DetailCard(widget: ResolvedPageWidget.DetailCard) {
    val compactHeaderActions = widget.actions.filter { action ->
        val compactLabel = action.compactLabel ?: return@filter false
        compactLabel.length == 1
    }
    val regularActions = widget.actions - compactHeaderActions.toSet()
    val colors = widget.tone.colors()
    val containerColor by animateColorAsState(
        targetValue = colors.container,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "detail_card_container_color",
    )
    val titleColor by animateColorAsState(
        targetValue = colors.title,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "detail_card_title_color",
    )
    val subtitleColor by animateColorAsState(
        targetValue = colors.subtitle,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "detail_card_subtitle_color",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (widget.enabled) 1f else 0.55f,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "detail_card_alpha",
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .then(
                if (widget.onClick != null && widget.enabled) {
                    Modifier.clickable(onClick = widget.onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = widget.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    modifier = Modifier.weight(1f),
                )
                if (compactHeaderActions.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        compactHeaderActions.forEach { action ->
                            CompactTrailingActionButton(action = action)
                        }
                    }
                }
            }
            widget.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                )
            }
            widget.rows.forEach { row ->
                ValueRow(
                    label = row.label,
                    value = row.value,
                )
            }
            if (regularActions.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    regularActions.forEach { action ->
                        RenderActionButton(
                            action = action,
                            modifier = Modifier.defaultMinSize(minWidth = 120.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(widget: ResolvedPageWidget.ProgressCard) {
    val colors = widget.tone.colors()
    val containerColor by animateColorAsState(
        targetValue = colors.container,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "progress_card_container_color",
    )
    val titleColor by animateColorAsState(
        targetValue = colors.title,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "progress_card_title_color",
    )
    val subtitleColor by animateColorAsState(
        targetValue = colors.subtitle,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "progress_card_subtitle_color",
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            widget.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                )
            }
            widget.progress.fraction?.let { fraction ->
                LinearProgressIndicator(
                    progress = { fraction.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            } ?: LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                gapSize = 0.dp,
            )
            widget.progress.label?.takeIf { it.isNotBlank() }?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = titleColor,
                )
            }
            widget.progress.supportingText?.takeIf { it.isNotBlank() }?.let { supportingText ->
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                )
            }
        }
    }
}

@Composable
private fun ChoiceCard(
    widget: ResolvedPageWidget.ChoiceCard,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val expandIconRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "choice_card_expand_icon_rotation",
    )
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = widget.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    widget.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.graphicsLayer(rotationZ = expandIconRotation),
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
                ) + expandVertically(
                    animationSpec = tween(durationMillis = MotionLong, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top,
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = MotionShort, easing = FastOutSlowInEasing),
                ) + shrinkVertically(
                    animationSpec = tween(durationMillis = MotionShort, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top,
                ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    widget.options.forEach { option ->
                        val optionContainerColor by animateColorAsState(
                            targetValue = if (!option.enabled) {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            } else if (option.selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
                            label = "choice_option_container_color",
                        )
                        val optionContentColor by animateColorAsState(
                            targetValue = if (!option.enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else if (option.selected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
                            label = "choice_option_content_color",
                        )
                        Card(
                            onClick = option.onClick,
                            enabled = option.enabled,
                            colors = CardDefaults.cardColors(
                                containerColor = optionContainerColor,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = optionContentColor,
                                    fontWeight = if (option.selected) FontWeight.Medium else FontWeight.Normal,
                                )
                                Text(
                                    text = if (option.selected) "•" else "›",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = optionContentColor,
                                )
                            }
                        }
                    }
                    if (widget.actions.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            widget.actions.forEach { action ->
                                RenderActionButton(
                                    action = action,
                                    modifier = Modifier.defaultMinSize(minWidth = 120.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleCard(widget: ResolvedPageWidget.ToggleCard) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (widget.enabled) 1f else 0.55f,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "toggle_card_alpha",
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = widget.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                widget.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(
                checked = widget.checked,
                onCheckedChange = if (widget.enabled) widget.onCheckedChange else null,
                enabled = widget.enabled,
            )
        }
    }
}

@Composable
private fun ButtonStack(actions: List<ResolvedPageAction>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.forEach { action ->
            RenderActionButton(
                action = action,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LaunchBar(widget: ResolvedPageWidget.LaunchBar) {
    val colors = widget.tone.colors()
    val containerColor by animateColorAsState(
        targetValue = colors.container,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "launch_bar_container_color",
    )
    val titleColor by animateColorAsState(
        targetValue = colors.title,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "launch_bar_title_color",
    )
    val subtitleColor by animateColorAsState(
        targetValue = colors.subtitle,
        animationSpec = tween(durationMillis = MotionMedium, easing = FastOutSlowInEasing),
        label = "launch_bar_subtitle_color",
    )
    val hasHeading = !widget.title.isNullOrBlank() || !widget.subtitle.isNullOrBlank()
    val compactMode = !hasHeading
    if (compactMode) {
        val leftActions = if (widget.secondaryActions.size <= 1) {
            emptyList()
        } else {
            widget.secondaryActions.take(1)
        }
        val rightActions = if (widget.secondaryActions.size <= 1) {
            widget.secondaryActions
        } else {
            widget.secondaryActions.drop(1)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (leftActions.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        leftActions.forEach { action ->
                            CompactTrailingActionButton(
                                action = action,
                                modifier = Modifier.defaultMinSize(minHeight = LaunchBarTrailingButtonSize),
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(LaunchBarTrailingButtonSize))
                }
            }
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center,
            ) {
                RenderActionButton(
                    action = widget.primaryAction,
                    modifier = Modifier.defaultMinSize(minWidth = 152.dp, minHeight = 40.dp),
                    singleLineLabel = true,
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (rightActions.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        rightActions.forEach { action ->
                            CompactTrailingActionButton(
                                action = action,
                                modifier = Modifier.defaultMinSize(minHeight = LaunchBarTrailingButtonSize),
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(LaunchBarTrailingButtonSize))
                }
            }
        }
        return
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = if (hasHeading) 12.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (hasHeading) 10.dp else 0.dp),
        ) {
            widget.title?.takeIf { it.isNotBlank() }?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                )
            }
            widget.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                )
            }
            RenderActionButton(
                action = widget.primaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 42.dp),
                singleLineLabel = true,
            )
            if (widget.secondaryActions.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    widget.secondaryActions.forEach { action ->
                        RenderActionButton(
                            action = action,
                            modifier = Modifier.defaultMinSize(minWidth = 108.dp, minHeight = 40.dp),
                            singleLineLabel = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTrailingActionButton(
    action: ResolvedPageAction,
    modifier: Modifier = Modifier,
) {
    val displayLabel = action.compactLabel ?: action.label
    if (displayLabel.length == 1) {
        IconButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = Modifier
                .size(LaunchBarTrailingButtonSize)
                .then(modifier)
                .semantics {
                    contentDescription = action.label
                },
        ) {
            Text(
                text = displayLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
        return
    }
    val compactModifier = modifier
        .widthIn(
            min = when {
                displayLabel.length <= 2 -> 60.dp
                else -> 72.dp
            },
        )
        .defaultMinSize(minHeight = 36.dp)
        .semantics {
            contentDescription = action.label
        }
    when (action.style) {
        PageActionStyle.FILLED_TONAL -> FilledTonalButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = compactModifier,
        ) {
            Text(
                text = displayLabel,
                fontSize = when {
                    displayLabel.length <= 1 -> 18.sp
                    displayLabel.length <= 2 -> 13.sp
                    else -> 12.sp
                },
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }

        PageActionStyle.OUTLINED -> OutlinedButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = compactModifier,
        ) {
            Text(
                text = displayLabel,
                fontSize = when {
                    displayLabel.length <= 1 -> 18.sp
                    displayLabel.length <= 2 -> 13.sp
                    else -> 12.sp
                },
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }

        PageActionStyle.TEXT -> TextButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = compactModifier,
        ) {
            Text(
                text = displayLabel,
                fontSize = when {
                    displayLabel.length <= 1 -> 18.sp
                    displayLabel.length <= 2 -> 13.sp
                    else -> 12.sp
                },
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun TextInputCard(
    nodeId: String,
    widget: ResolvedPageWidget.TextInputCard,
) {
    val focusManager = LocalFocusManager.current
    val bufferedInput = rememberBufferedTextInput(
        nodeId = nodeId,
        value = widget.value,
        onValueChange = widget.onValueChange,
    )
    var textInputFocused by remember(nodeId) { mutableStateOf(false) }
    DisposableEffect(nodeId) {
        onDispose {
            bufferedInput.commitNow()
            SystemBackCoordinator.clearTextInputFocus(nodeId)
        }
    }
    BindTextInputDismissHandler(enabled = textInputFocused) {
        focusManager.clearFocus(force = true)
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = bufferedInput.fieldValue,
                onValueChange = { value -> bufferedInput.update(value) },
                enabled = widget.enabled,
                singleLine = widget.singleLine,
                placeholder = widget.placeholder?.let { placeholder ->
                    { Text(placeholder) }
                },
                supportingText = widget.supportingText?.let { supportingText ->
                    { Text(supportingText) }
                },
                visualTransformation = if (widget.password) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        textInputFocused = focusState.isFocused || focusState.hasFocus
                        if (!textInputFocused) {
                            bufferedInput.commitNow()
                        }
                        SystemBackCoordinator.updateTextInputFocus(
                            nodeId = nodeId,
                            focused = textInputFocused,
                        )
                    }
                    .onPreviewKeyEvent { keyEvent ->
                        if (
                            keyEvent.type == KeyEventType.KeyDown &&
                            keyEvent.key == Key.Escape
                        ) {
                            focusManager.clearFocus(force = true)
                            true
                        } else {
                            false
                        }
                    },
            )
        }
    }
}

@Composable
private fun DirectoryInputCard(
    nodeId: String,
    widget: ResolvedPageWidget.DirectoryInputCard,
) {
    val focusManager = LocalFocusManager.current
    val bufferedInput = rememberBufferedTextInput(
        nodeId = nodeId,
        value = widget.value,
        onValueChange = widget.onValueChange,
    )
    var textInputFocused by remember(nodeId) { mutableStateOf(false) }
    DisposableEffect(nodeId) {
        onDispose {
            bufferedInput.commitNow()
            SystemBackCoordinator.clearTextInputFocus(nodeId)
        }
    }
    BindTextInputDismissHandler(enabled = textInputFocused) {
        focusManager.clearFocus(force = true)
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = widget.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = bufferedInput.fieldValue,
                onValueChange = { value -> bufferedInput.update(value) },
                enabled = widget.enabled,
                singleLine = true,
                placeholder = widget.placeholder?.let { placeholder ->
                    { Text(placeholder) }
                },
                supportingText = widget.supportingText?.let { supportingText ->
                    { Text(supportingText) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        textInputFocused = focusState.isFocused || focusState.hasFocus
                        if (!textInputFocused) {
                            bufferedInput.commitNow()
                        }
                        SystemBackCoordinator.updateTextInputFocus(
                            nodeId = nodeId,
                            focused = textInputFocused,
                        )
                    }
                    .onPreviewKeyEvent { keyEvent ->
                        if (
                            keyEvent.type == KeyEventType.KeyDown &&
                            keyEvent.key == Key.Escape
                        ) {
                            focusManager.clearFocus(force = true)
                            true
                        } else {
                            false
                        }
                    },
            )
            val pickDirectory = widget.onPickDirectory
            val pickButtonLabel = widget.pickButtonLabel
            if (widget.enabled && pickDirectory != null && !pickButtonLabel.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        pickDirectory(bufferedInput.fieldValue.text.takeIf { it.isNotBlank() }) { selected ->
                            val nextValue = selected?.trim().orEmpty()
                            if (nextValue.isNotEmpty()) {
                                bufferedInput.updateAndCommit(nextValue)
                            }
                        }
                    },
                ) {
                    Text(pickButtonLabel)
                }
            }
        }
    }
}

@Composable
private fun rememberBufferedTextInput(
    nodeId: String,
    value: String,
    onValueChange: (String) -> Unit,
): BufferedTextInputState {
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    var fieldValue by remember(nodeId) { mutableStateOf(value.toBufferedTextFieldValue()) }
    var committedText by remember(nodeId) { mutableStateOf(value) }

    fun commit(valueToCommit: String = fieldValue.text) {
        if (valueToCommit != committedText) {
            latestOnValueChange(valueToCommit)
            committedText = valueToCommit
        }
    }

    LaunchedEffect(nodeId, value) {
        if (value != committedText && value != fieldValue.text) {
            fieldValue = value.toBufferedTextFieldValue()
            committedText = value
        }
    }

    return BufferedTextInputState(
        fieldValue = fieldValue,
        update = { nextValue ->
            fieldValue = nextValue
        },
        updateAndCommit = { nextValue ->
            fieldValue = nextValue.toBufferedTextFieldValue()
            commit(nextValue)
        },
        commitNow = { commit() },
    )
}

private class BufferedTextInputState(
    val fieldValue: TextFieldValue,
    val update: (TextFieldValue) -> Unit,
    val updateAndCommit: (String) -> Unit,
    val commitNow: () -> Unit,
)

private fun String.toBufferedTextFieldValue(): TextFieldValue {
    return TextFieldValue(
        text = this,
        selection = TextRange(length),
    )
}

@Composable
private fun RenderActionButton(
    action: ResolvedPageAction,
    modifier: Modifier = Modifier,
    singleLineLabel: Boolean = false,
) {
    when (action.style) {
        PageActionStyle.FILLED_TONAL -> FilledTonalButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = modifier,
        ) {
            Text(
                text = action.label,
                maxLines = if (singleLineLabel) 1 else Int.MAX_VALUE,
                softWrap = !singleLineLabel,
                overflow = if (singleLineLabel) TextOverflow.Ellipsis else TextOverflow.Clip,
            )
        }

        PageActionStyle.OUTLINED -> OutlinedButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = modifier,
        ) {
            Text(
                text = action.label,
                maxLines = if (singleLineLabel) 1 else Int.MAX_VALUE,
                softWrap = !singleLineLabel,
                overflow = if (singleLineLabel) TextOverflow.Ellipsis else TextOverflow.Clip,
            )
        }

        PageActionStyle.TEXT -> TextButton(
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = modifier,
        ) {
            Text(
                text = action.label,
                maxLines = if (singleLineLabel) 1 else Int.MAX_VALUE,
                softWrap = !singleLineLabel,
                overflow = if (singleLineLabel) TextOverflow.Ellipsis else TextOverflow.Clip,
            )
        }
    }
}

private fun nodeRevealDelay(depth: Int, index: Int): Int {
    return (depth * 12 + index * 8).coerceAtMost(48)
}

private data class WidgetColors(
    val container: Color,
    val title: Color,
    val subtitle: Color,
)

@Composable
private fun PageWidgetTone.colors(): WidgetColors {
    return when (this) {
        PageWidgetTone.DEFAULT -> WidgetColors(
            container = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = MaterialTheme.colorScheme.onSurface,
            subtitle = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PageWidgetTone.ACCENT -> WidgetColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            title = MaterialTheme.colorScheme.onPrimaryContainer,
            subtitle = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        PageWidgetTone.DANGER -> WidgetColors(
            container = MaterialTheme.colorScheme.errorContainer,
            title = MaterialTheme.colorScheme.onErrorContainer,
            subtitle = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
