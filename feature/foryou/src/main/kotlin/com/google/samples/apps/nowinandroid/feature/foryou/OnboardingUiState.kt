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

import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic

/**
 * A sealed hierarchy describing the onboarding state for the for you screen.
 */

/// sealed interface/class:
//适用于需要定义多个类型，且这些类型之间有显著的结构差异的情况。
//适合用于需要表示复杂状态或行为的场景，比如 UI 状态、网络请求结果等。
//sealed 关键字用于定义一个封闭类层次结构。这意味着在同一个文件中，sealed 类或接口的所有子类必须被显式声明，不能在文件外部进行扩展。
//sealed 类或接口用于表示一个受限的继承层次结构，其中父类有一组有限的可能的子类。
//表达受限的可能性：sealed 类特别适合用来表示一组受限的状态或事件。例如，处理 API 响应、UI 状态或处理多种不同类型的错误时，可以使用 sealed 类来定义不同的情况。
//模式匹配：当你使用 sealed 类时，Kotlin 编译器可以知道所有可能的子类，这使得 when 表达式可以用来进行模式匹配，并且不需要 else 分支（只要你覆盖了所有子类）。

/***
 * sealed class ApiResponse {
 *     data class Success(val data: String) : ApiResponse()
 *     data class Error(val error: Throwable) : ApiResponse()
 *     object Loading : ApiResponse()
 * }
 *
 * fun handleResponse(response: ApiResponse) {
 *     when (response) {
 *         is ApiResponse.Success -> println("Data: ${response.data}")
 *         is ApiResponse.Error -> println("Error: ${response.error.message}")
 *         ApiResponse.Loading -> println("Loading...")
 *     }
 * }
 *
 *
 */

sealed interface OnboardingUiState {
    /**
     * The onboarding state is loading.
     */

    ///data 关键字用于定义数据类。数据类的主要目的是存储数据，而不是定义行为。
    //data 类自动生成一些常用方法，如 equals()、hashCode()、toString() 以及 copy() 方法，这些方法基于类的属性来实现。
    //存储数据：当你需要一个类来存储数据且需要对数据进行比较、打印或复制操作时，data 类是理想的选择。
    //不可变对象：通常 data 类与不可变对象（具有 val 属性）一起使用，以确保对象在创建后不会被修改。
    /**
     * data class User(val name: String, val age: Int)
     *
     * fun main() {
     *     val user1 = User("Alice", 25)
     *     val user2 = User("Alice", 25)
     *     println(user1 == user2)  // 输出: true，比较的是数据
     *     println(user1)           // 输出: User(name=Alice, age=25)，自动生成的 toString() 方法
     *     val user3 = user1.copy(age = 26)
     *     println(user3)           // 输出: User(name=Alice, age=26)，使用 copy() 方法创建新对象
     * }
     * */
    data object Loading : OnboardingUiState

    /**
     * The onboarding state was unable to load.
     */
    data object LoadFailed : OnboardingUiState

    /**
     * There is no onboarding state.
     */
    data object NotShown : OnboardingUiState

    /**
     * There is a onboarding state, with the given lists of topics.
     */
    data class Shown(
        val topics: List<FollowableTopic>,
    ) : OnboardingUiState {
        /**
         * True if the onboarding can be dismissed.
         */
        val isDismissable: Boolean get() = topics.any { it.isFollowed }
    }
}
