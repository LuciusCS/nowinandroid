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

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.samples.apps.nowinandroid.R
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.feature.bookmarks.R as bookmarksR
import com.google.samples.apps.nowinandroid.feature.foryou.R as forYouR
import com.google.samples.apps.nowinandroid.feature.search.R as searchR

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 *
 * enum class 是 Kotlin 中用于定义 枚举类型 的一种特殊类。枚举类型用于定义一组 有限的、固定的常量值，通常表示一个 状态集合 或 选项列表。每个枚举常量都是 枚举类型的实例。
 *
 *
 * enum class	用于定义枚举类，表示一组常量（例如 FOR_YOU、BOOKMARKS、INTERESTS）。
 *
 * 构造函数	enum class 可以有一个 主构造函数，每个枚举常量都可以通过构造函数初始化自己的属性。
 * 枚举常量	FOR_YOU、BOOKMARKS、INTERESTS 是该枚举类的常量，都是 TopLevelDestination 类的实例。
 * 属性	selectedIcon、unselectedIcon、iconTextId 和 titleTextId 是每个枚举常量的属性。
 *
 * 每个枚举常量（如 FOR_YOU）都是 TopLevelDestination 类的实例，并且可以拥有自己的属性和方法。
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    FOR_YOU(
        selectedIcon = NiaIcons.Upcoming,
        unselectedIcon = NiaIcons.UpcomingBorder,
        iconTextId = forYouR.string.feature_foryou_title,
        titleTextId = R.string.app_name,
    ),
    BOOKMARKS(
        selectedIcon = NiaIcons.Bookmarks,
        unselectedIcon = NiaIcons.BookmarksBorder,
        iconTextId = bookmarksR.string.feature_bookmarks_title,
        titleTextId = bookmarksR.string.feature_bookmarks_title,
    ),
    INTERESTS(
        selectedIcon = NiaIcons.Grid3x3,
        unselectedIcon = NiaIcons.Grid3x3,
        iconTextId = searchR.string.feature_search_interests,
        titleTextId = searchR.string.feature_search_interests,
    ),
}

/**
 * 3. enum class 的特性
 * （1）枚举常量是对象
 *
 * 每个枚举常量（如 FOR_YOU）都是 TopLevelDestination 类的实例，并且可以拥有自己的属性和方法。例如：
 *
 * val destination = TopLevelDestination.FOR_YOU
 * println(destination.selectedIcon) // 输出 NiaIcons.Upcoming
 * （2）可以实现接口
 *
 * enum class 可以实现接口，从而使每个枚举常量具有特定的行为。例如：
 *
 * interface Navigable {
 *     fun navigate(): String
 * }
 *
 * enum class TopLevelDestination : Navigable {
 *     FOR_YOU {
 *         override fun navigate() = "Navigating to For You"
 *     },
 *     BOOKMARKS {
 *         override fun navigate() = "Navigating to Bookmarks"
 *     },
 *     INTERESTS {
 *         override fun navigate() = "Navigating to Interests"
 *     }
 * }
 * （3）可以有方法和属性
 *
 * enum class 允许定义 方法和属性，并可以对每个枚举常量进行不同的实现：
 *
 * enum class TopLevelDestination {
 *     FOR_YOU,
 *     BOOKMARKS,
 *     INTERESTS;
 *
 *     fun printName() {
 *         println("Destination: $name") // `name` 是枚举常量的名称
 *     }
 * }
 * （4）伴生对象与静态方法
 *
 * 可以在 enum class 中定义 伴生对象 来实现类似于静态方法的功能：
 *
 * enum class TopLevelDestination {
 *     FOR_YOU,
 *     BOOKMARKS,
 *     INTERESTS;
 *
 *     companion object {
 *         fun fromName(name: String): TopLevelDestination? {
 *             return values().find { it.name == name }
 *         }
 *     }
 * }
 */

/**
 *
 *
 * 5. enum class 与 sealed class 的区别
 * 特性      	enum class	                      sealed class
 * 用途	    表示一组固定数量的常量	                 表示一组有限但不固定数量的子类
 * 继承关系	继承自 Enum，不支持继承其他类	          允许继承其他类或实现接口
 * 实例	    每个常量都是该枚举类的一个实例	         可以有多个不同类型的子类实例
 * 灵活性	固定的常量集合，适合枚举选项	              适合复杂的分支逻辑，可以携带不同类型的数据
 * 使用场景	状态、选项、固定集合（如导航目的地、颜色）	  不同的 UI 状态、复杂的业务逻辑
 *
 *
 *
 *
 *
 *
 */