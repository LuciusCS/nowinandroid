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

package com.google.samples.apps.nowinandroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.tracing.trace
import com.google.samples.apps.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.data.util.NetworkMonitor
import com.google.samples.apps.nowinandroid.core.data.util.TimeZoneMonitor
import com.google.samples.apps.nowinandroid.core.ui.TrackDisposableJank
import com.google.samples.apps.nowinandroid.feature.bookmarks.navigation.BOOKMARKS_ROUTE
import com.google.samples.apps.nowinandroid.feature.bookmarks.navigation.navigateToBookmarks
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.FOR_YOU_ROUTE
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.navigateToForYou
import com.google.samples.apps.nowinandroid.feature.interests.navigation.INTERESTS_ROUTE
import com.google.samples.apps.nowinandroid.feature.interests.navigation.navigateToInterests
import com.google.samples.apps.nowinandroid.feature.search.navigation.navigateToSearch
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.BOOKMARKS
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.FOR_YOU
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.INTERESTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

/**
 * @Composable: 标记为可组合函数。
 * rememberNiaAppState: 创建并返回一个 NiaAppState 实例，该实例管理应用的全局状态。
 * networkMonitor: 网络监控对象，用于跟踪应用的在线/离线状态。
 * userNewsResourceRepository: 用户新闻资源的仓库，用于获取和监控用户相关的新闻数据。
 * timeZoneMonitor: 时区监控对象，用于跟踪当前系统时区的变化。
 * coroutineScope: 协程作用域，用于在 NiaAppState 内部管理异步任务。
 * rememberCoroutineScope(): 记住当前的协程作用域，确保在重组过程中保持相同的作用域。
 * navController: 导航控制器，用于管理导航操作和后退栈。
 * rememberNavController(): 记住导航控制器，确保在重组过程中保持相同的控制器实例。
 *
 *
 *
 * 这个结构确保了应用在使用 Jetpack Compose 和 Navigation 时的状态管理和性能跟踪，并且适应了高效的 UI 构建和性能分析需求。
 *
 *
 */
@Composable
fun rememberNiaAppState(
    networkMonitor: NetworkMonitor,
    userNewsResourceRepository: UserNewsResourceRepository,
    timeZoneMonitor: TimeZoneMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): NiaAppState {
    /**
     * NavigationTrackingSideEffect(navController): 调用先前定义的 NavigationTrackingSideEffect 函数，跟踪导航事件。
     */
    NavigationTrackingSideEffect(navController)

    /**
     *
     * remember: 用于记住一个对象，确保在 Compose 重组期间保持相同的对象实例。将 navController、coroutineScope、networkMonitor、userNewsResourceRepository 和 timeZoneMonitor 作为依赖项。
     */

    return remember(
        navController,
        coroutineScope,
        networkMonitor,
        userNewsResourceRepository,
        timeZoneMonitor,
    ) {
        NiaAppState(
            navController = navController,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            userNewsResourceRepository = userNewsResourceRepository,
            timeZoneMonitor = timeZoneMonitor,
        )
    }
}

/***
 *
 * ChatGPT
 * 这个代码定义了一个名为 NiaAppState 的 Kotlin 类，用于管理应用程序的状态和导航逻辑，主要是在 Jetpack Compose 和 Navigation 组件中使用。
 *
 */

/**
 * @Stable: 注解表示该类是一个“稳定”的类，即如果该类的实例的值不变，那么它的状态不会改变，保证了可组合函数的高效重组。
 */

@Stable
class NiaAppState(
    /**
     * NavHostController 用于管理导航操作和后退堆栈。
     */
    val navController: NavHostController,
    /**
     * : CoroutineScope 用于在类内部启动协程。
     */
    coroutineScope: CoroutineScope,
    /**
     *  NetworkMonitor 用于监控网络状态。
     */
    networkMonitor: NetworkMonitor,
    /**
     * UserNewsResourceRepository 用于管理用户相关的新闻资源。
     */
    userNewsResourceRepository: UserNewsResourceRepository,
    /**
     * r: TimeZoneMonitor 用于监控当前系统时区。
     */
    timeZoneMonitor: TimeZoneMonitor,
) {

    /***
     * @Composable get(): 这是一个可组合的 getter，用于在 Compose 中获取当前导航目标。
     * currentBackStackEntryAsState(): 返回当前的后退堆栈条目（以状态形式），并获取其中的 destination。
     */
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    /**
     * currentTopLevelDestination: 返回当前顶层导航目标（如 FOR_YOU、BOOKMARKS、INTERESTS）。
     * when: 根据 currentDestination 的路由来判断当前是哪一个顶层目的地。
     */
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route) {
            FOR_YOU_ROUTE -> FOR_YOU
            BOOKMARKS_ROUTE -> BOOKMARKS
            INTERESTS_ROUTE -> INTERESTS
            else -> null
        }

    /**
     * isOffline: 一个 StateFlow，表示当前是否离线。
     * networkMonitor.isOnline: 流数据，表示网络是否在线。
     * map(Boolean::not): 将 isOnline 转换为其反值，即 isOffline。
     * stateIn: 将流数据转换为 StateFlow，并将其作用于给定的协程作用域。
     * SharingStarted.WhileSubscribed(5_000): 控制 StateFlow 的共享模式，表示在最后一个订阅者断开连接后，最多保持5秒钟的流数据。
     */
    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Map of top level destinations to be used in the TopBar, BottomBar and NavRail. The key is the
     * route
     * 顶层导航目标的列表，通常用于顶部栏、底部栏或导航栏中。
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * The top level destinations that have unread news resources.
     * topLevelDestinationsWithUnreadResources: 一个 StateFlow，表示当前具有未读资源的顶层导航目标。
     * observeAllForFollowedTopics() 和 observeAllBookmarked(): 观察用户关注的主题和已书签的资源。
     * combine: 组合两个流数据，返回一个新的流数据。
     * setOfNotNull: 创建一个不包含 null 的集合。
     * stateIn: 同样将流数据转换为 StateFlow。
     */
    val topLevelDestinationsWithUnreadResources: StateFlow<Set<TopLevelDestination>> =
        userNewsResourceRepository.observeAllForFollowedTopics()
            .combine(userNewsResourceRepository.observeAllBookmarked()) { forYouNewsResources, bookmarkedNewsResources ->
                setOfNotNull(
                    FOR_YOU.takeIf { forYouNewsResources.any { !it.hasBeenViewed } },
                    BOOKMARKS.takeIf { bookmarkedNewsResources.any { !it.hasBeenViewed } },
                )
            }
            .stateIn(
                coroutineScope,
                SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet(),
            )

    val currentTimeZone = timeZoneMonitor.currentTimeZone
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            TimeZone.currentSystemDefault(),
        )

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     *
     * @param topLevelDestination: The destination the app needs to navigate to.
     *
     * navigateToTopLevelDestination: 导航到指定的顶层导航目标。
     * trace: 用于调试和性能跟踪的代码块。
     * navOptions: 创建导航选项。
     * popUpTo: 回退到导航图的起始目的地，以避免堆栈中有多个相同的目的地。
     * launchSingleTop: 确保不会在堆栈中创建多个相同的顶层目的地实例。
     * restoreState: 在重新选择以前选中的顶层目的地时恢复状态。
     * when: 根据传入的 topLevelDestination，调用相应的导航方法。
     */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }

            when (topLevelDestination) {
                FOR_YOU -> navController.navigateToForYou(topLevelNavOptions)
                BOOKMARKS -> navController.navigateToBookmarks(topLevelNavOptions)
                INTERESTS -> navController.navigateToInterests(null, topLevelNavOptions)
            }
        }
    }

    /**
     * navigateToSearch: 导航到搜索页面。
     */
    fun navigateToSearch() = navController.navigateToSearch()
}

/**
 * Stores information about navigation events to be used with JankStats
 * @Composable: 标记函数为可组合函数，可以在 Compose UI 树中使用。
 * NavigationTrackingSideEffect: 定义一个私有的可组合函数，用于跟踪导航事件。
 */
@Composable
private fun NavigationTrackingSideEffect(navController: NavHostController) {

    /**
     *
     * TrackDisposableJank: 这是一个用于跟踪应用程序中 jank（性能滞后）事件的函数。传递 navController 并提供一个 lambda 回调，内含 metricsHolder。
     * metricsHolder: 一个对象，可能包含性能度量信息，用于跟踪导航相关的性能问题。
     */

    TrackDisposableJank(navController) { metricsHolder ->

        /**
         * listener: 创建一个导航监听器，当导航目的地发生变化时触发。
         * destination: 当前导航目标，destination.route 是其对应的路由。
         * metricsHolder.state?.putState: 更新 metricsHolder 中的状态，将当前导航路由保存为 "Navigation" 状态，用于性能跟踪。
         */
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            metricsHolder.state?.putState("Navigation", destination.route.toString())
        }

        /**
         * addOnDestinationChangedListener: 为 navController 添加先前定义的 listener，以便监听导航事件。
         */
        navController.addOnDestinationChangedListener(listener)

        /**
         * onDispose: 用于清理资源。当这个可组合函数不再处于活跃状态时，移除导航监听器，以防止内存泄漏。
         *
         */
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}
