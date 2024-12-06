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

package com.google.samples.apps.nowinandroid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.google.samples.apps.nowinandroid.feature.bookmarks.navigation.bookmarksScreen
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.FOR_YOU_ROUTE
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.forYouScreen
import com.google.samples.apps.nowinandroid.feature.interests.navigation.navigateToInterests
import com.google.samples.apps.nowinandroid.feature.search.navigation.searchScreen
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.INTERESTS
import com.google.samples.apps.nowinandroid.ui.NiaAppState
import com.google.samples.apps.nowinandroid.ui.interests2pane.interestsListDetailScreen

/**
 * Top-level navigation graph. Navigation is organized as explained at
 * https://d.android.com/jetpack/compose/nav-adaptive
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun NiaNavHost(
    appState: NiaAppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = FOR_YOU_ROUTE,
) {
    val navController = appState.navController
    /**
     *
     *
     * 为什么 NavHost 可以使用 forYouScreen 和 bookmarksScreen 这种形式调用？
     *
     * 核心概念：
     * forYouScreen 和 bookmarksScreen 是扩展函数（Extension Functions），通过 扩展 NavGraphBuilder 实现。
     * 因此，它们可以直接在 NavHost 的 lambda 中调用，类似于直接调用 composable 函数。
     *
     * 详细解释
     * 1. NavHost 与 NavGraphBuilder
     *
     * NavHost 是一个 @Composable 函数，负责根据 NavController 当前的状态来显示不同的屏幕。
     * NavHost 的第二个参数 是一个 NavGraphBuilder 的 lambda，用于构建导航图（NavGraph）。
     * NavHost(
     *     navController = navController,
     *     startDestination = startDestination,
     * ) {
     *     // 这里是 NavGraphBuilder 的 lambda
     *     // 你可以调用 `NavGraphBuilder` 的扩展函数，例如 composable()，也可以自定义扩展函数，例如 forYouScreen()
     * }
     * 2. forYouScreen 是 NavGraphBuilder 的扩展函数
     *
     * fun NavGraphBuilder.forYouScreen(onTopicClick: (String) -> Unit) {
     *     composable(
     *         route = FOR_YOU_ROUTE,
     *         deepLinks = listOf(
     *             navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
     *         ),
     *         arguments = listOf(
     *             navArgument(LINKED_NEWS_RESOURCE_ID) { type = NavType.StringType },
     *         ),
     *     ) {
     *         ForYouRoute(onTopicClick)
     *     }
     * }
     * 定义了一个扩展函数 forYouScreen，扩展了 NavGraphBuilder，这意味着：
     * 你可以在 NavHost 的 lambda 中直接调用 forYouScreen，就像调用 composable 一样。
     * 这个函数内部实际上是通过 composable() 函数来定义导航目标的。
     * 3. 为什么 forYouScreen 可以直接在 NavHost 中使用？
     *
     * 因为 forYouScreen 是 NavGraphBuilder 的扩展函数，而 NavHost 的 lambda 接收的是一个 NavGraphBuilder 对象。因此：
     *
     * forYouScreen 作为 NavGraphBuilder 的扩展函数，可以在 NavHost 中直接调用。
     * 4. 完整调用过程：
     *
     * NavHost 创建一个 NavGraphBuilder。
     * NavHost 内的 lambda 实际上接收了这个 NavGraphBuilder 实例。
     * 在 lambda 中调用 forYouScreen()，它是 NavGraphBuilder 的扩展函数。
     * forYouScreen 内部通过 composable() 定义导航目标，最终构建了完整的导航图。
     * 总结
     * NavHost 接收了一个 NavGraphBuilder，可以使用 composable()、navigation() 等函数来构建导航图。
     * forYouScreen 是 NavGraphBuilder 的扩展函数，因此可以像 composable() 一样在 NavHost 内部直接调用。
     * 通过扩展函数，能将不同的导航逻辑分离为多个函数，提高代码的 可读性 和 复用性。
     *
     *
     */
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {

        /**
         * NavHost 中的参数 builder: androidx.navigation.NavGraphBuilder.() -> kotlin.Unit)
         *
         * 这里是 NavGraphBuilder 的 lambda
         * 你可以调用 `NavGraphBuilder` 的扩展函数，例如 composable()，也可以自定义扩展函数，例如 forYouScreen()
         *
         *
         * builder 参数是 NavGraphBuilder 的扩展函数类型的 Lambda，这意味着：
         *
         * 在 builder 中，this 的类型是 NavGraphBuilder。
         * 只要是 NavGraphBuilder 的扩展函数 或者内部定义的函数，都可以直接调用。
         * 因此，你可以在 builder 中调用多个方法（如 forYouScreen()、bookmarksScreen()、searchScreen() 等），因为它们都扩展自 NavGraphBuilder。
         *
         *
         * 在 Jetpack Compose 和 Navigation 组件结合使用时，NavHost是导航的核心部分，它会根据导航状态来显示不同的Composable。
         *  当NavHost调用多个Composable时，它实际上是根据导航目的地（destination）来决定显示哪个Composable。
         *  每个Composable就像是一个独立的屏幕或者页面片段，NavHost起到了一个容器和调度器的作用，
         *  根据用户的导航操作（如点击按钮切换页面）在这些Composable之间进行切换。
         *
         */

        forYouScreen(onTopicClick = navController::navigateToInterests)
        bookmarksScreen(
            onTopicClick = navController::navigateToInterests,
            onShowSnackbar = onShowSnackbar,
        )
        searchScreen(
            onBackClick = navController::popBackStack,
            onInterestsClick = { appState.navigateToTopLevelDestination(INTERESTS) },
            onTopicClick = navController::navigateToInterests,
        )
        interestsListDetailScreen()
    }
}

/**
 * NavHost 如何调用 composable()
 * NavHost 是 Jetpack Compose 中用于管理导航的容器，通过 composable() 函数来定义每个屏幕（或目的地）的内容。你可以在 NavHost 内部调用 composable() 来定义导航路径和每个路径对应的 UI 组件。
 *
 * 基本使用方式
 * 1. 定义 NavHost 并使用 composable() 函数
 *
 * NavHost(
 *     navController = navController,
 *     startDestination = "home", // 起始目的地
 *     modifier = Modifier.fillMaxSize()
 * ) {
 *     composable("home") {
 *         HomeScreen() // 显示 HomeScreen 组件
 *     }
 *     composable("details/{itemId}",
 *         arguments = listOf(navArgument("itemId") { type = NavType.StringType })
 *     ) { backStackEntry ->
 *         val itemId = backStackEntry.arguments?.getString("itemId")
 *         DetailsScreen(itemId) // 显示 DetailsScreen 组件，并将参数传递过去
 *     }
 * }
 * composable() 的参数详解
 * 1. route
 *
 * 必填参数，表示导航路径。
 * 可以是静态字符串路径，如 "home"。
 * 也可以是带有参数的动态路径，如 "details/{itemId}"。
 * 2. arguments
 *
 * 可选参数，表示导航目的地接收的参数。
 * 使用 navArgument() 指定参数名称和类型。
 * composable(
 *     route = "details/{itemId}",
 *     arguments = listOf(navArgument("itemId") { type = NavType.StringType })
 * )
 * 3. deepLinks
 *
 * 可选参数，表示支持的深度链接（Deep Link）。
 * 通过 navDeepLink { uriPattern = "myapp://details/{itemId}" } 定义。
 * composable(
 *     route = "details/{itemId}",
 *     deepLinks = listOf(navDeepLink { uriPattern = "myapp://details/{itemId}" })
 * )
 * 4. content
 *
 * 必填参数，表示导航到此目的地时要显示的 Composable 内容。
 * composable("home") {
 *     HomeScreen() // 显示 HomeScreen 组件
 * }
 * 完整示例：使用多个 composable() 定义导航
 * @Composable
 * fun MyApp(navController: NavHostController) {
 *     NavHost(
 *         navController = navController,
 *         startDestination = "home"
 *     ) {
 *         composable("home") {
 *             HomeScreen(
 *                 onNavigateToDetails = { itemId ->
 *                     navController.navigate("details/$itemId")
 *                 }
 *             )
 *         }
 *         composable(
 *             route = "details/{itemId}",
 *             arguments = listOf(navArgument("itemId") { type = NavType.StringType })
 *         ) { backStackEntry ->
 *             val itemId = backStackEntry.arguments?.getString("itemId")
 *             DetailsScreen(itemId)
 *         }
 *     }
 * }
 *
 * @Composable
 * fun HomeScreen(onNavigateToDetails: (String) -> Unit) {
 *     Column {
 *         Text(text = "Home Screen")
 *         Button(onClick = { onNavigateToDetails("123") }) {
 *             Text(text = "Go to Details")
 *         }
 *     }
 * }
 *
 * @Composable
 * fun DetailsScreen(itemId: String?) {
 *     Text(text = "Details Screen for Item ID: $itemId")
 * }
 * 动态导航的过程
 * 导航到 DetailsScreen：
 * 在 HomeScreen 中调用 navController.navigate("details/123")。
 * 接收参数：
 * DetailsScreen 使用 backStackEntry.arguments?.getString("itemId") 获取 itemId 参数的值。
 * 渲染对应的 UI。
 * 总结
 * NavHost 用于定义导航容器。
 * composable() 是 NavGraphBuilder 的扩展函数，用于定义导航路径与 Composable 之间的映射。
 * 通过 arguments 和 deepLinks，可以定义复杂的导航逻辑，包括参数传递和深度链接。
 *
 *
 */
