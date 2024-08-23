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

package com.google.samples.apps.nowinandroid.feature.foryou.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.samples.apps.nowinandroid.feature.foryou.ForYouRoute

///代码定义了一个用于导航到 "For You" 界面的导航规则，支持传递参数和处理深度链接。


/// 表示在导航过程中传递的参数名。它将在路径或深度链接中使用。
const val LINKED_NEWS_RESOURCE_ID = "linkedNewsResourceId"
///这个占位符表示该路由需要一个动态参数
const val FOR_YOU_ROUTE = "for_you_route/{$LINKED_NEWS_RESOURCE_ID}"

///定义深度链接 URI 模式的常量 DEEP_LINK_URI_PATTERN。这个模式对应的链接可以触发应用内导航，并将 LINKED_NEWS_RESOURCE_ID 作为参数传递。
private const val DEEP_LINK_URI_PATTERN =
    "https://www.nowinandroid.apps.samples.google.com/foryou/{$LINKED_NEWS_RESOURCE_ID}"

/// 导航到 FOR_YOU_ROUTE 对应的界面。它接受 NavOptions 作为参数，以便设置导航选项（如过渡动画、返回堆栈等）。
fun NavController.navigateToForYou(navOptions: NavOptions) = navigate(FOR_YOU_ROUTE, navOptions)


///定义一个扩展函数 forYouScreen，用于在 NavGraphBuilder 中配置一个新屏幕。onTopicClick 是一个高阶函数，接收 String 类型参数，通常用于处理点击事件。
fun NavGraphBuilder.forYouScreen(onTopicClick: (String) -> Unit) {

    ///通过 NavGraphBuilder中的composable 函数定义一个新的可组合项（Composable）
    composable(
        route = FOR_YOU_ROUTE,   ///指定导航路径，这里使用了 FOR_YOU_ROUTE。
        deepLinks = listOf(    ///指定深度链接配置，uriPattern 对应上面定义的 DEEP_LINK_URI_PATTERN，用于处理外部链接
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),
        arguments = listOf(  ///指定导航参数，这里是 LINKED_NEWS_RESOURCE_ID，它的类型为 String。
            navArgument(LINKED_NEWS_RESOURCE_ID) { type = NavType.StringType },
        ),
    ) {
        ForYouRoute(onTopicClick)   ///在导航到这个路由时，展示 ForYouRoute 组件，并将 onTopicClick 回调传递给它，用于处理点击事件。
    }
}
