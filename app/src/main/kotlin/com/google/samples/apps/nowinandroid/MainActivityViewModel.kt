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

package com.google.samples.apps.nowinandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.MainActivityUiState.Loading
import com.google.samples.apps.nowinandroid.MainActivityUiState.Success
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.model.data.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/***
 *
 * @HiltViewModel：这是Dagger-Hilt的注解，表示这个ViewModel可以通过Hilt进行依赖注入。Hilt会自动管理ViewModel的生命周期和依赖关系。
 *
 * class MainActivityViewModel @Inject constructor(...): ViewModel()：定义了MainActivityViewModel类，它继承自ViewModel。
 * 通过@Inject constructor，Hilt会自动注入userDataRepository依赖。
 *
 * userDataRepository: UserDataRepository：MainActivityViewModel的构造函数依赖于UserDataRepository，这个仓库类负责提供用户数据。
 */

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
) : ViewModel() {
    /**
     * 这是一个StateFlow类型的属性，用于公开当前的UI状态。StateFlow是一个持有状态的可观察流，当状态改变时会发出新的数据。
     */
    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userData.map {

        /**
         * userDataRepository.userData.map { Success(it) }：
         * 从userDataRepository获取用户数据流（Flow<UserData>），并将其映射为MainActivityUiState.Success状态。
         * .map { Success(it) }：map操作符用于将用户数据包装成Success状态。
         */

        Success(it)

        /**
         *
         * .stateIn(...)：stateIn是一个终端操作符，将普通的Flow转换为StateFlow。它允许我们指定作用域、初始值和流的启动策略。
         */

    }.stateIn(
        /**
         * scope = viewModelScope：指定ViewModel的协程范围（viewModelScope）作为流的生命周期管理器，确保流在ViewModel销毁时自动取消。
         */
        scope = viewModelScope,
        /**
         * initialValue = Loading：指定StateFlow的初始值为Loading状态，表示数据正在加载中。
         */
        initialValue = Loading,
        /**
         * started = SharingStarted.WhileSubscribed(5_000)：控制流的启动和停止策略。在没有订阅者时停止收集，
         * 当有新的订阅者时重新启动。5,000毫秒的延迟允许流在短时间内保留状态，避免频繁的重启。
         */
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

/**
 *
 *
 * Kotlin中的sealed关键字
 * sealed（密封）类或接口在Kotlin中是一种特殊的类/接口，它限制了子类的定义范围。使用sealed关键字的类或接口，
 * 其所有的直接子类都必须定义在同一个文件中。这种设计可以让你清楚地知道所有可能的子类类型，提供了更好的类型安全性和可预测性。
 *
 * sealed关键字的作用
 * 限制继承：sealed类的所有子类都必须定义在与sealed类相同的文件中。这样可以确保你能够枚举出所有可能的子类，避免未知的子类出现在别的文件中。
 * 代替枚举（Enum）：当一个类型有多种可能的状态时，你可以使用sealed类代替枚举，因为sealed类允许为每种状态传递不同的数据，而枚举只能携带少量信息。
 * 模式匹配的完整性：当使用sealed类时，Kotlin的when表达式会自动检查是否处理了所有可能的子类。这有助于在编译时保证代码的完整性。
 *
 *
 *
 *
 *
 * sealed interface MainActivityUiState：定义一个密封接口MainActivityUiState，用于表示UI的不同状态。密封类/接口的子类必须在同一个文件中声明，这种方式确保所有可能的状态都被穷尽。
 */
sealed interface MainActivityUiState {
    /**
     * data object Loading : MainActivityUiState：定义一个对象类型的状态Loading，表示数据加载中。这里使用data object，因为Kotlin 1.9 引入了data object来表示一个只有单一实例的数据类。
     */
    data object Loading : MainActivityUiState

    /**
     * data class Success(val userData: UserData) : MainActivityUiState：定义一个数据类Success，表示成功状态，并包含userData数据。这种状态表明数据已经加载成功并可用于UI展示。
     */
    data class Success(val userData: UserData) : MainActivityUiState
}

/**
 *
 *
 * Kotlin中的sealed关键字
 * sealed（密封）类或接口在Kotlin中是一种特殊的类/接口，它限制了子类的定义范围。使用sealed关键字的类或接口，其所有的直接子类都必须定义在同一个文件中。
 * 这种设计可以让你清楚地知道所有可能的子类类型，提供了更好的类型安全性和可预测性。
 *
 * sealed关键字的作用
 * 限制继承：sealed类的所有子类都必须定义在与sealed类相同的文件中。这样可以确保你能够枚举出所有可能的子类，避免未知的子类出现在别的文件中。
 * 代替枚举（Enum）：当一个类型有多种可能的状态时，你可以使用sealed类代替枚举，因为sealed类允许为每种状态传递不同的数据，而枚举只能携带少量信息。
 * 模式匹配的完整性：当使用sealed类时，Kotlin的when表达式会自动检查是否处理了所有可能的子类。这有助于在编译时保证代码的完整性。
 * 用法
 * 定义sealed类
 *
 * kotlin
 * Copy code
 * sealed class Response
 * data class Success(val data: String) : Response()
 * data class Error(val errorCode: Int, val message: String) : Response()
 * object Loading : Response()
 * 在这个例子中，Response是一个sealed类，它有三个子类：Success、Error、和Loading。
 *
 * 使用when表达式匹配sealed类
 *
 * kotlin
 * Copy code
 * fun handleResponse(response: Response) {
 *     when (response) {
 *         is Success -> println("Data: ${response.data}")
 *         is Error -> println("Error ${response.errorCode}: ${response.message}")
 *         Loading -> println("Loading...")
 *     }
 * }
 * 在这个when表达式中，Kotlin会自动检查是否涵盖了Response的所有子类。如果遗漏了某个子类，编译器会发出警告。
 *
 * data object的用法
 * data object是Kotlin 1.9中引入的一个特性，它结合了object（单例）和data class的特点，用于表示一个单例数据对象。这种对象只会有一个实例，但它可以是data，即可以自动生成toString()、equals()、和hashCode()等方法。
 *
 * 为什么sealed类中可以定义data object
 *
 * 在sealed类中，data object可以用来表示一种状态或场景，而这种状态在应用程序中只会有一个实例。例如，在sealed类中使用data object来表示某种“唯一”状态，比如加载中、空数据状态等。
 *
 * kotlin
 * Copy code
 * sealed interface UiState {
 *     data object Loading : UiState
 *     data object Empty : UiState
 *     data class Success(val data: List<String>) : UiState
 * }
 * 在这个例子中，Loading和Empty是data object，它们表示不同的UI状态，且只会有一个实例。
 *
 * 其他用法及场景
 * 表示多种结果类型
 *
 * sealed类常用于表示具有多种可能结果的操作结果。例如，网络请求可以返回成功、错误或加载状态。
 *
 * kotlin
 * Copy code
 * sealed class ApiResponse<out T> {
 *     data class Success<out T>(val data: T) : ApiResponse<T>()
 *     data class Failure(val error: Throwable) : ApiResponse<Nothing>()
 *     object Loading : ApiResponse<Nothing>()
 * }
 * 表示复杂的UI状态
 *
 * 在处理复杂的UI逻辑时，sealed类可以用于描述不同的UI状态。
 *
 * kotlin
 * Copy code
 * sealed class UiState {
 *     object Loading : UiState()
 *     data class Success(val data: String) : UiState()
 *     data class Error(val message: String) : UiState()
 * }
 * 代替枚举类型
 *
 * 如果需要为每种状态携带不同类型的数据，sealed类是枚举的更灵活的替代方案。
 *
 * kotlin
 * Copy code
 * sealed class OrderStatus {
 *     object Pending : OrderStatus()
 *     object Shipped : OrderStatus()
 *     data class Delivered(val deliveryDate: String) : OrderStatus()
 *     data class Cancelled(val reason: String) : OrderStatus()
 * }
 * 总结
 * sealed类用于限制继承，使得所有子类都必须在同一文件中定义，提供了类型安全性和可预测性。
 * sealed类适用于需要表达多个状态或结果的场景，常用于UI状态、API响应等场景。
 * data object用于表示具有data特性的单例对象，适合表示唯一状态。
 * 使用sealed类配合when表达式，可以确保处理所有可能的状态，有助于编写更安全的代码。
 *
 *
 */