/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.feature.foryou

import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tracing.trace
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus.Denied
import com.google.accompanist.permissions.rememberPermissionState
import com.google.samples.apps.nowinandroid.core.designsystem.component.DynamicAsyncImage
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaButton
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaIconToggleButton
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaOverlayLoadingWheel
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DecorativeScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.core.ui.TrackScrollJank
import com.google.samples.apps.nowinandroid.core.ui.UserNewsResourcePreviewParameterProvider
import com.google.samples.apps.nowinandroid.core.ui.launchCustomChromeTab
import com.google.samples.apps.nowinandroid.core.ui.newsFeed

@Composable   /// @Composable：注解，表示这是一个可组合函数，用于声明 UI。
internal fun ForYouRoute(   ///internal  修饰符，表示该函数只能在当前模块内访问。
    onTopicClick: (String) -> Unit,   ///一个高阶函数参数，用于处理主题点击事件。String 参数是点击的主题的 ID 或名称。
    modifier: Modifier = Modifier,  ///一个 Modifier 参数，用于配置 UI 元素的布局、行为和外观。默认值为 Modifier 的空实例。
    viewModel: ForYouViewModel = hiltViewModel(),  ///ForYouViewModel 是一个 ViewModel 实例，使用 hiltViewModel() 函数自动注入 ViewModel 实例。它负责管理 UI 状态和业务逻辑。
) {

    ///使用 collectAsStateWithLifecycle() 从 viewModel.onboardingUiState 中收集状态。
    // onboardingUiState 是一个表示引导界面状态的 Flow。
    ///关键字用于委托属性，使 onboardingUiState 委托给 Flow 的状态更新。
    val onboardingUiState by viewModel.onboardingUiState.collectAsStateWithLifecycle()

    /// 收集 viewModel.feedState 的状态。feedState 表示当前新闻资源的状态（如加载中、加载完成等）。
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    ///收集 viewModel.isSyncing 的状态，表示应用是否正在与远程数据同步。
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    ///收集 viewModel.deepLinkedNewsResource 的状态，表示通过深度链接获取的特定新闻资源。
    val deepLinkedUserNewsResource by viewModel.deepLinkedNewsResource.collectAsStateWithLifecycle()

    ForYouScreen(
        isSyncing = isSyncing,
        onboardingUiState = onboardingUiState,
        feedState = feedState,
        deepLinkedUserNewsResource = deepLinkedUserNewsResource,
        onTopicCheckedChanged = viewModel::updateTopicSelection,
        onDeepLinkOpened = viewModel::onDeepLinkOpened,
        onTopicClick = onTopicClick,
        saveFollowedTopics = viewModel::dismissOnboarding,
        onNewsResourcesCheckedChanged = viewModel::updateNewsResourceSaved,
        onNewsResourceViewed = { viewModel.setNewsResourceViewed(it, true) },
        modifier = modifier,
    )
}

@Composable
internal fun ForYouScreen(
    isSyncing: Boolean,
    onboardingUiState: OnboardingUiState,
    feedState: NewsFeedUiState,
    deepLinkedUserNewsResource: UserNewsResource?,
    onTopicCheckedChanged: (String, Boolean) -> Unit,
    onTopicClick: (String) -> Unit,
    onDeepLinkOpened: (String) -> Unit,
    saveFollowedTopics: () -> Unit,
    onNewsResourcesCheckedChanged: (String, Boolean) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    // 检查 Onboarding 状态是否正在加载
    val isOnboardingLoading = onboardingUiState is OnboardingUiState.Loading


    //  // 检查 Feed 状态是否正在加载
    val isFeedLoading = feedState is NewsFeedUiState.Loading

    // This code should be called when the UI is ready for use and relates to Time To Full Display.
    // // 当 UI 准备好显示时调用 ReportDrawnWhen 方法
    //    // 这个方法通常用于记录从启动到完全显示的时间 (Time To Full Display, TTFD)
    ReportDrawnWhen { !isSyncing && !isOnboardingLoading && !isFeedLoading }

    ////  // 计算当前可用的 feed 项数
    val itemsAvailable = feedItemsSize(feedState, onboardingUiState)
    // 创建并记住一个 LazyStaggeredGridState 用于管理懒加载网格的滚动状态
    val state = rememberLazyStaggeredGridState()
    //   // 使用 scrollableState 为滚动状态创建一个 scrollbarState
    val scrollbarState = state.scrollbarState(
        itemsAvailable = itemsAvailable,
    )
    // 跟踪滚动卡顿 (Jank) 的发生，以便在性能分析中提供数据
    TrackScrollJank(scrollableState = state, stateName = "forYou:feed")

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(300.dp),   ///允许列的宽度自适应，列宽至少为 300.dp，如果屏幕宽度允许，它将会添加更多列以填满可用空间。
            contentPadding = PaddingValues(16.dp),  /// 为整个网格内容设置内边距（padding），即内容与父容器边界之间的距离。
            horizontalArrangement = Arrangement.spacedBy(16.dp), ///设置网格中的列之间的水平间距，即每列之间的间隔为 16.dp。
            verticalItemSpacing = 24.dp,
            modifier = Modifier
                .testTag("forYou:feed"),
            state = state,  ///将滚动状态传递给 LazyVerticalStaggeredGrid，通常用来管理和控制滚动行为。
        ) {
            onboarding(
                onboardingUiState = onboardingUiState,   ///传递当前的 onboardingUiState 状态给 onboarding 函数，以根据状态显示不同的内容。
                onTopicCheckedChanged = onTopicCheckedChanged,  /// 传递一个函数回调，处理主题选择状态的变化。
                saveFollowedTopics = saveFollowedTopics,  //传递一个函数回调，处理保存关注的主题。
                // Custom LayoutModifier to remove the enforced parent 16.dp contentPadding
                // from the LazyVerticalGrid and enable edge-to-edge scrolling for this section
                ///自定义 Modifier，通过手动测量和布局来调整元素的宽度。这里通过扩展约束的最大宽度（maxWidth + 32.dp.roundToPx()）来实现取消 LazyVerticalGrid 强制的父级 16.dp 内边距，并启用边到边的滚动效果。
                interestsItemModifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        constraints.copy(
                            maxWidth = constraints.maxWidth + 32.dp.roundToPx(),
                        ),
                    )
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0)
                    }
                },
            )

            newsFeed(
                feedState = feedState,  ///: 传递当前的 feedState 状态给 newsFeed 函数，以根据状态显示新闻内容。
                onNewsResourcesCheckedChanged = onNewsResourcesCheckedChanged,
                onNewsResourceViewed = onNewsResourceViewed,
                onTopicClick = onTopicClick,
            )

            ///添加一个占据整个网格宽度的项目。span = StaggeredGridItemSpan.FullLine 表示这个项目将跨越所有列，
            // contentType = "bottomSpacing" 用于标识这个项目的内容类型，以便于布局和优化。
            item(span = StaggeredGridItemSpan.FullLine, contentType = "bottomSpacing") {

                /// 这个项目的内容是一个 Column，用于垂直排列子项。
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Add space for the content to clear the "offline" snackbar.
                    // TODO: Check that the Scaffold handles this correctly in NiaApp
                    // if (isOffline) Spacer(modifier = Modifier.height(48.dp))
                    ///添加一个根据系统窗口插图高度调整的空白间距，确保内容不会与系统 UI（如导航栏）重叠。
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                }
            }
        }
        AnimatedVisibility(
            visible = isSyncing || isFeedLoading || isOnboardingLoading,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> -fullHeight },
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
            ) + fadeOut(),
        ) {
            val loadingContentDescription = stringResource(id = R.string.feature_foryou_loading)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                NiaOverlayLoadingWheel(
                    modifier = Modifier
                        .align(Alignment.Center),
                    contentDesc = loadingContentDescription,
                )
            }
        }
        state.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterEnd),
            state = scrollbarState,
            orientation = Orientation.Vertical,
            onThumbMoved = state.rememberDraggableScroller(
                itemsAvailable = itemsAvailable,
            ),
        )
    }

    /// // 跟踪屏幕的查看事件 (screen view event)，用于分析用户行为
    TrackScreenViewEvent(screenName = "ForYou")

    ///    // 请求通知权限的副作用
    NotificationPermissionEffect()

    /// // 处理深度链接的副作用
    DeepLinkEffect(
        deepLinkedUserNewsResource,
        onDeepLinkOpened,
    )
}

/**
 * An extension on [LazyListScope] defining the onboarding portion of the for you screen.
 * Depending on the [onboardingUiState], this might emit no items.
 *
 */
private fun LazyStaggeredGridScope.onboarding(
    onboardingUiState: OnboardingUiState,
    onTopicCheckedChanged: (String, Boolean) -> Unit,
    saveFollowedTopics: () -> Unit,
    interestsItemModifier: Modifier = Modifier,
) {
    when (onboardingUiState) {
        OnboardingUiState.Loading,
        OnboardingUiState.LoadFailed,
        OnboardingUiState.NotShown,
        -> Unit

        is OnboardingUiState.Shown -> {
            item(span = StaggeredGridItemSpan.FullLine, contentType = "onboarding") {
                Column(modifier = interestsItemModifier) {
                    Text(
                        text = stringResource(R.string.feature_foryou_onboarding_guidance_title),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.feature_foryou_onboarding_guidance_subtitle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 24.dp, end = 24.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TopicSelection(
                        onboardingUiState,
                        onTopicCheckedChanged,
                        Modifier.padding(bottom = 8.dp),
                    )
                    // Done button
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        NiaButton(
                            onClick = saveFollowedTopics,
                            enabled = onboardingUiState.isDismissable,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .widthIn(364.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                text = stringResource(R.string.feature_foryou_done),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicSelection(
    onboardingUiState: OnboardingUiState.Shown,
    onTopicCheckedChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) = trace("TopicSelection") {
    val lazyGridState = rememberLazyGridState()
    val topicSelectionTestTag = "forYou:topicSelection"

    TrackScrollJank(scrollableState = lazyGridState, stateName = topicSelectionTestTag)

    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        LazyHorizontalGrid(
            state = lazyGridState,
            rows = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(24.dp),
            modifier = Modifier
                // LazyHorizontalGrid has to be constrained in height.
                // However, we can't set a fixed height because the horizontal grid contains
                // vertical text that can be rescaled.
                // When the fontScale is at most 1, we know that the horizontal grid will be at most
                // 240dp tall, so this is an upper bound for when the font scale is at most 1.
                // When the fontScale is greater than 1, the height required by the text inside the
                // horizontal grid will increase by at most the same factor, so 240sp is a valid
                // upper bound for how much space we need in that case.
                // The maximum of these two bounds is therefore a valid upper bound in all cases.
                .heightIn(max = max(240.dp, with(LocalDensity.current) { 240.sp.toDp() }))
                .fillMaxWidth()
                .testTag(topicSelectionTestTag),
        ) {
            items(
                items = onboardingUiState.topics,
                key = { it.topic.id },
            ) {
                SingleTopicButton(
                    name = it.topic.name,
                    topicId = it.topic.id,
                    imageUrl = it.topic.imageUrl,
                    isSelected = it.isFollowed,
                    onClick = onTopicCheckedChanged,
                )
            }
        }
        lazyGridState.DecorativeScrollbar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .align(Alignment.BottomStart),
            state = lazyGridState.scrollbarState(itemsAvailable = onboardingUiState.topics.size),
            orientation = Orientation.Horizontal,
        )
    }
}

@Composable
private fun SingleTopicButton(
    name: String,
    topicId: String,
    imageUrl: String,
    isSelected: Boolean,
    onClick: (String, Boolean) -> Unit,
) = trace("SingleTopicButton") {
    Surface(
        modifier = Modifier
            .width(312.dp)
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        color = MaterialTheme.colorScheme.surface,
        selected = isSelected,
        onClick = {
            onClick(topicId, !isSelected)
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
        ) {
            TopicIcon(
                imageUrl = imageUrl,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
            )
            NiaIconToggleButton(
                checked = isSelected,
                onCheckedChange = { checked -> onClick(topicId, checked) },
                icon = {
                    Icon(
                        imageVector = NiaIcons.Add,
                        contentDescription = name,
                    )
                },
                checkedIcon = {
                    Icon(
                        imageVector = NiaIcons.Check,
                        contentDescription = name,
                    )
                },
            )
        }
    }
}

@Composable
fun TopicIcon(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    DynamicAsyncImage(
        placeholder = painterResource(R.drawable.feature_foryou_ic_icon_placeholder),
        imageUrl = imageUrl,
        // decorative
        contentDescription = null,
        modifier = modifier
            .padding(10.dp)
            .size(32.dp),
    )
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun NotificationPermissionEffect() {
    // Permission requests should only be made from an Activity Context, which is not present
    // in previews
    if (LocalInspectionMode.current) return
    if (VERSION.SDK_INT < VERSION_CODES.TIRAMISU) return
    val notificationsPermissionState = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS,
    )
    LaunchedEffect(notificationsPermissionState) {
        val status = notificationsPermissionState.status
        if (status is Denied && !status.shouldShowRationale) {
            notificationsPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun DeepLinkEffect(
    userNewsResource: UserNewsResource?,
    onDeepLinkOpened: (String) -> Unit,
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

    LaunchedEffect(userNewsResource) {
        if (userNewsResource == null) return@LaunchedEffect
        if (!userNewsResource.hasBeenViewed) onDeepLinkOpened(userNewsResource.id)

        launchCustomChromeTab(
            context = context,
            uri = Uri.parse(userNewsResource.url),
            toolbarColor = backgroundColor,
        )
    }
}

private fun feedItemsSize(
    feedState: NewsFeedUiState,
    onboardingUiState: OnboardingUiState,
): Int {
    val feedSize = when (feedState) {
        NewsFeedUiState.Loading -> 0
        is NewsFeedUiState.Success -> feedState.feed.size
    }
    val onboardingSize = when (onboardingUiState) {
        OnboardingUiState.Loading,
        OnboardingUiState.LoadFailed,
        OnboardingUiState.NotShown,
        -> 0

        is OnboardingUiState.Shown -> 1
    }
    return feedSize + onboardingSize
}

@DevicePreviews
@Composable
fun ForYouScreenPopulatedFeed(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NiaTheme {
        ForYouScreen(
            isSyncing = false,
            onboardingUiState = OnboardingUiState.NotShown,
            feedState = NewsFeedUiState.Success(
                feed = userNewsResources,
            ),
            deepLinkedUserNewsResource = null,
            onTopicCheckedChanged = { _, _ -> },
            saveFollowedTopics = {},
            onNewsResourcesCheckedChanged = { _, _ -> },
            onNewsResourceViewed = {},
            onTopicClick = {},
            onDeepLinkOpened = {},
        )
    }
}

@DevicePreviews
@Composable
fun ForYouScreenOfflinePopulatedFeed(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NiaTheme {
        ForYouScreen(
            isSyncing = false,
            onboardingUiState = OnboardingUiState.NotShown,
            feedState = NewsFeedUiState.Success(
                feed = userNewsResources,
            ),
            deepLinkedUserNewsResource = null,
            onTopicCheckedChanged = { _, _ -> },
            saveFollowedTopics = {},
            onNewsResourcesCheckedChanged = { _, _ -> },
            onNewsResourceViewed = {},
            onTopicClick = {},
            onDeepLinkOpened = {},
        )
    }
}

@DevicePreviews
@Composable
fun ForYouScreenTopicSelection(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NiaTheme {
        ForYouScreen(
            isSyncing = false,
            onboardingUiState = OnboardingUiState.Shown(
                topics = userNewsResources.flatMap { news -> news.followableTopics }
                    .distinctBy { it.topic.id },
            ),
            feedState = NewsFeedUiState.Success(
                feed = userNewsResources,
            ),
            deepLinkedUserNewsResource = null,
            onTopicCheckedChanged = { _, _ -> },
            saveFollowedTopics = {},
            onNewsResourcesCheckedChanged = { _, _ -> },
            onNewsResourceViewed = {},
            onTopicClick = {},
            onDeepLinkOpened = {},
        )
    }
}

@DevicePreviews
@Composable
fun ForYouScreenLoading() {
    NiaTheme {
        ForYouScreen(
            isSyncing = false,
            onboardingUiState = OnboardingUiState.Loading,
            feedState = NewsFeedUiState.Loading,
            deepLinkedUserNewsResource = null,
            onTopicCheckedChanged = { _, _ -> },
            saveFollowedTopics = {},
            onNewsResourcesCheckedChanged = { _, _ -> },
            onNewsResourceViewed = {},
            onTopicClick = {},
            onDeepLinkOpened = {},
        )
    }
}

@DevicePreviews
@Composable
fun ForYouScreenPopulatedAndLoading(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NiaTheme {
        ForYouScreen(
            isSyncing = true,
            onboardingUiState = OnboardingUiState.Loading,
            feedState = NewsFeedUiState.Success(
                feed = userNewsResources,
            ),
            deepLinkedUserNewsResource = null,
            onTopicCheckedChanged = { _, _ -> },
            saveFollowedTopics = {},
            onNewsResourcesCheckedChanged = { _, _ -> },
            onNewsResourceViewed = {},
            onTopicClick = {},
            onDeepLinkOpened = {},
        )
    }
}
