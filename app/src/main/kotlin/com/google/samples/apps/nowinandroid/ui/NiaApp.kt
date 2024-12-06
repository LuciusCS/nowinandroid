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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.google.samples.apps.nowinandroid.R
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaBackground
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaGradientBackground
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaNavigationSuiteScaffold
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaTopAppBar
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.GradientColors
import com.google.samples.apps.nowinandroid.core.designsystem.theme.LocalGradientColors
import com.google.samples.apps.nowinandroid.feature.settings.SettingsDialog
import com.google.samples.apps.nowinandroid.navigation.NiaNavHost
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination
import com.google.samples.apps.nowinandroid.feature.settings.R as settingsR

/***
 *
 * 在 Jetpack Compose 中，NiaApp(appState) 可以只传入 appState 而不需要显式传入 modifier 和 windowAdaptiveInfo，这是因为 Kotlin 提供了默认参数机制 和 函数参数的调用方式。
 *
 * 1. 默认参数（Default Parameters）
 * fun NiaApp(
 *     appState: NiaAppState,
 *     modifier: Modifier = Modifier,
 *     windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
 * )
 * modifier: Modifier = Modifier
 * modifier 参数有一个默认值 Modifier，表示一个空的 Modifier，因此调用 NiaApp(appState) 时，如果没有显式传递 modifier，它会使用默认值。
 * windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
 * windowAdaptiveInfo 也有一个默认值 currentWindowAdaptiveInfo()，这是一个函数调用，返回当前窗口的自适应信息。
 * 因此，如果不传递该参数，它会自动调用 currentWindowAdaptiveInfo() 来生成 windowAdaptiveInfo 的默认值。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NiaApp(
    appState: NiaAppState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val shouldShowGradientBackground =
        appState.currentTopLevelDestination == TopLevelDestination.FOR_YOU
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    NiaBackground(modifier = modifier) {
        NiaGradientBackground(
            gradientColors = if (shouldShowGradientBackground) {
                LocalGradientColors.current
            } else {
                GradientColors()
            },
        ) {

            /**
             * 这段代码的作用是在 Jetpack Compose 中 记住并管理 SnackbarHostState 对象的状态，以确保在 Composable 重组（Recomposition）时不会重新创建该对象。
             *
             * SnackbarHostState：是一个用于管理 Snackbar 显示和隐藏状态的类，通常与 SnackbarHost 组件一起使用，用来控制 Snackbar（即消息提示框）的显示。
             * remember { ... }：是 Jetpack Compose 提供的 API，用来在 Composable 的生命周期内缓存对象。它的核心作用是 在重组时复用已有的对象，避免每次重组都重新创建。
             *
             *
             *
             * 3. 为什么要使用 remember？
             * 如果不使用 remember，可能会导致 SnackbarHostState 在每次重组时被重新创建，从而引发以下问题：
             *
             * （1）状态丢失
             *
             * SnackbarHostState 负责管理 Snackbar 的显示状态。如果每次重组都创建一个新的 SnackbarHostState 实例：
             * Snackbar 显示过程中，重组会导致 Snackbar 瞬间消失，因为新创建的 SnackbarHostState 不知道之前的状态。
             * 用户的交互状态也会丢失，影响用户体验。
             * （2）资源浪费
             *
             * 每次重组都会重新分配内存来创建新的 SnackbarHostState 对象，增加不必要的资源消耗和内存开销，降低性能。
             * 使用 remember 后：
             *
             * SnackbarHostState 只会在首次创建时生成，之后的重组中都会复用这个实例。
             * 状态（例如 Snackbar 是否显示）能够在重组之间保持一致。
             */
            val snackbarHostState = remember { SnackbarHostState() }

            val isOffline by appState.isOffline.collectAsStateWithLifecycle()

            // If user is not connected to the internet show a snack bar to inform them.
            val notConnectedMessage = stringResource(R.string.not_connected)
            /**
             * LaunchedEffect 是一个用于运行副作用（side effects）的 API，专门设计用来响应 Compose 中的状态变化。它的主要特点是：
             *
             * 它是一个挂起函数，可以用于运行长时间操作，如网络请求、显示 Snackbar 等。
             * 它与 Composable 的生命周期相关联。只有当其键（key 参数）发生变化时，它才会重新启动。
             *
             * 当 isOffline 的值改变时，这个 LaunchedEffect 会重新启动，并显示一条 Snackbar。
             *
             *
             * 为什么LaunchedEffect与NiaApp可以同时运行？
             * 线程和上下文不同：
             * LaunchedEffect 是挂起函数，运行在 Compose 提供的协程作用域中，负责处理副作用。
             * NiaApp 是一个纯粹的 UI 渲染函数，负责绘制视图。
             * 它们之间没有直接冲突，因为一个是用于处理逻辑操作（协程），另一个是用于描述界面（Composable）。
             * Compose 的状态驱动机制：
             * isOffline 是一个由 State 驱动的值，Compose 会根据它的值来触发不同的行为：
             * 触发 LaunchedEffect，显示 Snackbar。
             * 重新绘制 UI（如 NiaApp）。
             * Compose 会智能地决定在必要时更新哪些部分，确保不会重复运行不必要的代码。
             * 声明式编程模型：
             * 在声明式 UI 中，你描述 "当某个状态满足条件时应该发生什么"。
             * LaunchedEffect 和 NiaApp 都是响应同一个状态（isOffline）的变化，但它们的目的和逻辑分离，因此可以同时运行。
             *
             *
             *
             * LaunchedEffect 和 DisposableEffect 在 Jetpack Compose 中都与 副作用 处理有关，但它们的用法和目的略有不同。以下是它们的对比和适用场景：
             *
             * 相同点
             * 与 Composable 生命周期相关联：
             * 它们都依赖于 Composable 的生命周期，并在其键（key 参数）发生变化时重新启动或清理。
             * 都是用来处理某些需要在 Composable 外部运行的逻辑。
             * 必须在 Composable 函数中使用：
             * 两者都只能在 Composable 函数的上下文中使用。
             * 不同点
             * 特性	LaunchedEffect	DisposableEffect
             *
             *
             * 不同点
             * 特性	        LaunchedEffect	                            DisposableEffect
             * 主要用途	    执行挂起操作或协程操作。	                    管理需要清理的资源或监听器。
             * 生命周期	    在 Composable 的 key 改变时重新启动协程。	    在 key 改变时清理旧资源并重新初始化资源。
             * 清理逻辑	    没有专门设计清理逻辑，需要手动管理。          	提供内置的清理逻辑（通过返回的 lambda）。
             * 运行上下文    	在 Compose 提供的协程作用域中运行。          	在 Composable 的生命周期中运行。
             * 典型应用场景   	- 加载数据                                   - 注册/注销监听器
             *                  - 显示 Snackbar                             - 初始化资源
             *                  - 动画操作	                                - 生命周期清理
             * 支持挂起操作	是	                                         否
             *
             *
             *
             * isOffline 的切换：
             * 如果 isOffline 的状态由某种不稳定的网络检测逻辑控制，且网络状态波动频繁（比如短时间内切换多次），就会导致 LaunchedEffect 不断重新执行。
             *
             */
            LaunchedEffect(isOffline) {
                if (isOffline) {
                    snackbarHostState.showSnackbar(
                        message = notConnectedMessage,
                        duration = Indefinite,
                    )
                }
            }

            /**
             * 调用的是 internal fun NiaApp(){}，而不是自身
             *
             *
             * 在 Kotlin 中，这种 方法同名 而不冲突的现象是因为它们有不同的签名（函数的参数列表不同），这称为 函数重载（Function Overloading）
             *
             * Kotlin 支持 函数重载，即允许在同一个类、文件或作用域中定义多个同名函数，前提是它们的 函数签名不同。函数签名由以下部分决定：
             *
             * 函数名
             * 参数类型和数量
             * 因此，尽管这两个函数的名字都叫 NiaApp，但它们的参数列表不同，所以编译器可以区分它们，并不会认为是递归调用。
             *
             * 两个 NiaApp 函数名字相同，但参数列表不同，因此是合法的重载，不会冲突。
             * Kotlin 编译器会根据调用时的参数来选择合适的函数进行调用，而不会认为是递归调用。
             * 这种设计方式通常用于将 UI 层（@Composable 函数）和逻辑层（普通函数）分离，方便代码组织和复用。
             */
            NiaApp(
                appState = appState,
                snackbarHostState = snackbarHostState,
                showSettingsDialog = showSettingsDialog,
                onSettingsDismissed = { showSettingsDialog = false },
                onTopAppBarActionClick = { showSettingsDialog = true },
                windowAdaptiveInfo = windowAdaptiveInfo,
            )
        }
    }
}

/**
 *
 * internal 是一种 可见性修饰符，它用于控制类、函数、属性、或者对象的可见性。其作用是让被修饰的成员 只能在同一个模块内访问。
 */
@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
internal fun NiaApp(
    appState: NiaAppState,
    snackbarHostState: SnackbarHostState,
    showSettingsDialog: Boolean,
    onSettingsDismissed: () -> Unit,
    onTopAppBarActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val unreadDestinations by appState.topLevelDestinationsWithUnreadResources
        .collectAsStateWithLifecycle()
    val currentDestination = appState.currentDestination

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { onSettingsDismissed() },
        )
    }

    NiaNavigationSuiteScaffold(
        navigationSuiteItems = {
            appState.topLevelDestinations.forEach { destination ->
                val hasUnread = unreadDestinations.contains(destination)
                val selected = currentDestination
                    .isTopLevelDestinationInHierarchy(destination)
                item(
                    selected = selected,
                    onClick = { appState.navigateToTopLevelDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = destination.selectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.iconTextId)) },
                    modifier =
                    Modifier
                        .testTag("NiaNavItem")
                        .then(if (hasUnread) Modifier.notificationDot() else Modifier),
                )
            }
        },
        windowAdaptiveInfo = windowAdaptiveInfo,
    ) {
        Scaffold(
            modifier = modifier.semantics {
                testTagsAsResourceId = true
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) {
            /***
             *
             *    Scaffold  类似于 Flutter中的 Scaffold
             *
             *
             *   参数中有 topBar: @Composable () -> Unit = {},
             *    bottomBar: @Composable () -> Unit = {},
             *    但是在此处用的自定义的方式
             *
             *
             */


            padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal,
                        ),
                    ),
            ) {

                // Show the top app bar on top level destinations.
                val destination = appState.currentTopLevelDestination
                val shouldShowTopAppBar = destination != null
                if (destination != null) {
                    NiaTopAppBar(
                        titleRes = destination.titleTextId,
                        navigationIcon = NiaIcons.Search,
                        navigationIconContentDescription = stringResource(
                            id = settingsR.string.feature_settings_top_app_bar_navigation_icon_description,
                        ),
                        actionIcon = NiaIcons.Settings,
                        actionIconContentDescription = stringResource(
                            id = settingsR.string.feature_settings_top_app_bar_action_icon_description,
                        ),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                        onActionClick = { onTopAppBarActionClick() },
                        onNavigationClick = { appState.navigateToSearch() },
                    )
                }

                Box(
                    // Workaround for https://issuetracker.google.com/338478720
                    modifier = Modifier.consumeWindowInsets(
                        if (shouldShowTopAppBar) {
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        } else {
                            WindowInsets(0, 0, 0, 0)
                        },
                    ),
                ) {
                    NiaNavHost(
                        appState = appState,
                        onShowSnackbar = { message, action ->
                            snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = action,
                                duration = Short,
                            ) == ActionPerformed
                        },
                    )
                }

                // TODO: We may want to add padding or spacer when the snackbar is shown so that
                //  content doesn't display behind it.
            }
        }
    }
}

private fun Modifier.notificationDot(): Modifier =
    composed {
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        drawWithContent {
            drawContent()
            drawCircle(
                tertiaryColor,
                radius = 5.dp.toPx(),
                // This is based on the dimensions of the NavigationBar's "indicator pill";
                // however, its parameters are private, so we must depend on them implicitly
                // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
                center = center + Offset(
                    64.dp.toPx() * .45f,
                    32.dp.toPx() * -.45f - 6.dp.toPx(),
                ),
            )
        }
    }

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
