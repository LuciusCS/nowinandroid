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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsEvent
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsEvent.Param
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsHelper
import com.google.samples.apps.nowinandroid.core.data.repository.NewsResourceQuery
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.data.util.SyncManager
import com.google.samples.apps.nowinandroid.core.domain.GetFollowableTopicsUseCase
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.LINKED_NEWS_RESOURCE_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel   ///@HiltViewModel：注解，表示这个 ViewModel 是通过 Dagger Hilt 管理的。
class ForYouViewModel @Inject constructor(   /// @inject 使用依赖注入来获取 ViewModel 需要的依赖项。
    private val savedStateHandle: SavedStateHandle,   ///保存和恢复 UI 状态的句柄。
    /**
     *  * 当Hilt看到ForYouViewModel需要一个SyncManager时，
     *  * 它会在依赖关系图中查找哪些类实现了SyncManager接口。如果只有一个类实现了该接口，
     *  * 并且这个类（如WorkManagerSyncManager）的构造函数上有@Inject注解，Hilt会自动实例化WorkManagerSyncManager
     *  * 并将其注入到syncManager参数中。
     */

    syncManager: SyncManager,   ///用于管理数据同步状态。注入的是 WorkManagerSyncManager  ，WorkManagerSyncManager是SyncManager的实现
    private val analyticsHelper: AnalyticsHelper,   ///用于记录分析事件的帮助类。
    private val userDataRepository: UserDataRepository,  ///用户数据仓库，用于处理用户相关的数据。
    userNewsResourceRepository: UserNewsResourceRepository,  ///新闻资源仓库，用于处理新闻资源的数据。
    getFollowableTopics: GetFollowableTopicsUseCase,   ///用例类，用于获取用户可以关注的主题。
) : ViewModel() {

    ////shouldShowOnboarding：这是一个 Flow<Boolean>，表示是否应该显示引导界面。通过映射用户数据中的 shouldHideOnboarding 字段来确定是否隐藏引导界面。
    private val shouldShowOnboarding: Flow<Boolean> =
        userDataRepository.userData.map { !it.shouldHideOnboarding }

    ///这是一个 StateFlow，表示通过深度链接获取的特定新闻资源。
    val deepLinkedNewsResource = savedStateHandle.getStateFlow<String?>(  ///从 SavedStateHandle 中获取带有默认值的状态流。
        key = LINKED_NEWS_RESOURCE_ID,
        null,
    )
        .flatMapLatest { newsResourceId ->   //// 当深度链接中的新闻资源 ID 变化时，使用新的 ID 查询对应的新闻资源。
            if (newsResourceId == null) {
                flowOf(emptyList())
            } else {
                userNewsResourceRepository.observeAll(
                    NewsResourceQuery(
                        filterNewsIds = setOf(newsResourceId),
                    ),
                )
            }
        }
        .map { it.firstOrNull() }///获取查询结果中的第一个新闻资源，若无结果则返回 null。
        .stateIn(     ///将 Flow 转换为 StateFlow，并在 ViewModel 的生命周期内管理其状态。
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    ////这是一个 StateFlow<Boolean>，表示数据是否正在同步。它通过 SyncManager 来获取同步状态。
    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    ///  StateFlow<NewsFeedUiState>，表示新闻资源的 UI 状态。
    val feedState: StateFlow<NewsFeedUiState> =
        userNewsResourceRepository.observeAllForFollowedTopics()  ///从新闻资源仓库中获取所有已关注主题的新闻资源。
            .map(NewsFeedUiState::Success)   ///将新闻资源转换为 Success 状态。
            .stateIn(   /// 将结果包装为 StateFlow，初始值为 Loading 状态。
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsFeedUiState.Loading,
            )

    ///StateFlow<OnboardingUiState>，表示引导界面的状态。
    val onboardingUiState: StateFlow<OnboardingUiState> =
        combine(   /////将多个 Flow 合并为一个 Flow，根据是否显示引导界面和可关注主题来确定 OnboardingUiState。
            shouldShowOnboarding,
            getFollowableTopics(),
        ) { shouldShowOnboarding, topics ->
            if (shouldShowOnboarding) {
                OnboardingUiState.Shown(topics = topics)
            } else {
                OnboardingUiState.NotShown
            }
        }
            .stateIn(    ///将结果包装为 StateFlow，初始值为 Loading 状态。
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = OnboardingUiState.Loading,
            )

    /// 用于更新用户对某个主题的选择状态（关注或取消关注）。
    fun updateTopicSelection(topicId: String, isChecked: Boolean) {
        viewModelScope.launch {  ///在 ViewModel 的作用域内启动一个协程，异步执行更新操作。
            userDataRepository.setTopicIdFollowed(topicId, isChecked)
        }
    }

    fun updateNewsResourceSaved(newsResourceId: String, isChecked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, isChecked)
        }
    }

    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }

    fun onDeepLinkOpened(newsResourceId: String) {
        if (newsResourceId == deepLinkedNewsResource.value?.id) {
            savedStateHandle[LINKED_NEWS_RESOURCE_ID] = null
        }
        analyticsHelper.logNewsDeepLinkOpen(newsResourceId = newsResourceId)
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(
                newsResourceId = newsResourceId,
                viewed = true,
            )
        }
    }

    fun dismissOnboarding() {
        viewModelScope.launch {
            userDataRepository.setShouldHideOnboarding(true)
        }
    }
}

private fun AnalyticsHelper.logNewsDeepLinkOpen(newsResourceId: String) =
    logEvent(
        AnalyticsEvent(
            type = "news_deep_link_opened",
            extras = listOf(
                Param(
                    key = LINKED_NEWS_RESOURCE_ID,
                    value = newsResourceId,
                ),
            ),
        ),
    )
