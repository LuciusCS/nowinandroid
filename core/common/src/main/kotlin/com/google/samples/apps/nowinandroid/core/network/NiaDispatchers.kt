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

package com.google.samples.apps.nowinandroid.core.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME



@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val niaDispatcher: NiaDispatchers)

enum class NiaDispatchers {
    Default,
    IO,
}

/**
 *
 *
 *
 * Dispatcher 注解是一个自定义的 Qualifier 注解，用于标识和区分不同的依赖项。在依赖注入框架中，比如 Dagger 或 Dagger Hilt，Qualifier 注解用于解决依赖注入中的冲突问题，当多个相同类型的依赖项存在时，可以使用 Qualifier 来进行区分。
 *
 * 代码分析
 * kotlin
 * Copy code
 * @Qualifier
 * @Retention(RUNTIME)
 * annotation class Dispatcher(val niaDispatcher: NiaDispatchers)
 * @Qualifier: 这个注解表明 Dispatcher 是一个 Qualifier 注解，用于在依赖注入时区分不同的依赖项。
 *
 * @Retention(RUNTIME): 这个注解表示 Dispatcher 注解的保留策略是 RUNTIME，即它在运行时也可见。这是必要的，因为依赖注入框架需要在运行时读取这个注解来确定要注入哪个依赖项。
 *
 * annotation class Dispatcher(val niaDispatcher: NiaDispatchers): 这是自定义的 Dispatcher 注解。它接收一个 NiaDispatchers 枚举类型的参数，允许我们指定使用哪种调度器。
 *
 * 枚举 NiaDispatchers
 * kotlin
 * Copy code
 * enum class NiaDispatchers {
 *     Default,
 *     IO,
 * }
 * NiaDispatchers 是一个枚举类，定义了两种调度器类型：Default 和 IO。这些可以用来表示不同的 CoroutineDispatcher，例如默认调度器或 IO 调度器。
 * 注解的作用与生效
 * 1. 定义注入的依赖项
 * 通常，我们会在提供不同 CoroutineDispatcher 的方法上使用这个 Dispatcher 注解，以区分不同类型的调度器：
 *
 * kotlin
 * Copy code
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object CoroutinesModule {
 *
 *     @Provides
 *     @Dispatcher(NiaDispatchers.Default)
 *     fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
 *
 *     @Provides
 *     @Dispatcher(NiaDispatchers.IO)
 *     fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
 * }
 * 在这个例子中，我们提供了两种不同的 CoroutineDispatcher：
 *
 * Dispatchers.Default 通过 @Dispatcher(NiaDispatchers.Default) 被标识为默认调度器。
 * Dispatchers.IO 通过 @Dispatcher(NiaDispatchers.IO) 被标识为 IO 调度器。
 * 2. 注入依赖项
 * 当我们需要注入某个调度器时，我们可以使用 @Dispatcher 注解来指定要注入的具体调度器：
 *
 * kotlin
 * Copy code
 * class MyRepository @Inject constructor(
 *     @Dispatcher(NiaDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
 * ) {
 *     fun someFunction() {
 *         CoroutineScope(ioDispatcher).launch {
 *             // 这里的协程将在 IO 线程上执行
 *         }
 *     }
 * }
 * @Inject 注解用于自动注入依赖项。
 * @Dispatcher(NiaDispatchers.IO) 用于指定注入的是 IO 调度器，而不是 Default 调度器。
 * 如何生效
 * 注入时区分依赖: 在依赖注入的过程中，Dagger Hilt 会根据 @Dispatcher(NiaDispatchers.Default) 和 @Dispatcher(NiaDispatchers.IO) 的标识，选择注入对应的 CoroutineDispatcher 实例。也就是说，哪个调度器被注入取决于你在注入时使用的 Qualifier 注解的参数。
 *
 * 运行时的依赖选择: 由于 @Dispatcher 注解的保留策略是 RUNTIME，依赖注入框架在运行时能够读取并解析这个注解，从而确保注入的依赖项是符合预期的。
 *
 * 总结
 * Dispatcher 注解通过 @Qualifier 来标识和区分不同的 CoroutineDispatcher，并在依赖注入过程中根据你指定的枚举值 NiaDispatchers 来注入正确的依赖项。这种机制非常有助于管理复杂的依赖关系，特别是在使用多个相同类型但不同配置的依赖项时。
 *
 *
 */

/**
 *
 * 因为 @Dispatcher 是一个自定义的注解，并不像 Dagger Hilt 的内建注解那样直接与框架内部逻辑绑定。那么，Dagger Hilt 是如何理解并根据 @Dispatcher 注解来解析依赖项的呢？我们来详细解释一下这个过程。
 *
 * 1. Dagger Hilt 如何处理 Qualifier 注解
 * Dagger Hilt 依赖 Dagger 作为底层依赖注入框架。Dagger 支持 @Qualifier 注解，用于在依赖注入中标识不同的依赖项。
 *
 * @Qualifier: 是一个标准的注解，Dagger 在依赖注入时使用它来区分不同的依赖项。你可以用它创建自定义的限定符（Qualifier），如 @Dispatcher，以此来为同一类型的依赖提供不同的实现。
 * 当 Dagger 看到 @Qualifier 注解时，它会将其视为一个标识符，用于在注入点和提供方法之间进行匹配。具体到你的场景中，@Dispatcher(NiaDispatchers.Default) 和 @Dispatcher(NiaDispatchers.IO) 将被用于匹配对应的提供方法。
 *
 * 2. 匹配依赖项和提供方法
 * 来看这个例子：
 *
 * kotlin
 * Copy code
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object CoroutinesModule {
 *
 *     @Provides
 *     @Dispatcher(NiaDispatchers.Default)
 *     fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
 *
 *     @Provides
 *     @Dispatcher(NiaDispatchers.IO)
 *     fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
 * }
 * provideDefaultDispatcher() 方法被 @Dispatcher(NiaDispatchers.Default) 标记。
 * provideIODispatcher() 方法被 @Dispatcher(NiaDispatchers.IO) 标记。
 * 当 Dagger Hilt 需要注入一个 CoroutineDispatcher 时，它会检查注入点上使用的 @Qualifier 注解（即 @Dispatcher(NiaDispatchers.Default) 或 @Dispatcher(NiaDispatchers.IO)），并根据这个标识符去寻找具有相同标识符的提供方法。
 *
 * 例如：
 *
 * kotlin
 * Copy code
 * class MyRepository @Inject constructor(
 *     @Dispatcher(NiaDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
 * )
 * 在这个例子中，Dagger Hilt 看到 @Dispatcher(NiaDispatchers.IO)，然后会找到 @Dispatcher(NiaDispatchers.IO) 对应的提供方法，即 provideIODispatcher()，并将其结果注入到 ioDispatcher 中。
 *
 * 3. 自定义注解的解析
 * 虽然 @Dispatcher 是自定义的，但它与 @Qualifier 绑定，使 Dagger 能够理解并使用它。Dagger 解析注解时，会查看该注解是否是一个 @Qualifier，如果是，就将该注解视为依赖项的标识符，并在生成的代码中使用这个标识符来选择正确的依赖项。
 *
 * Dagger 的代码生成
 * Dagger 生成的代码会根据注解的匹配情况来决定哪个提供方法与哪个注入点对应。自定义 @Qualifier 注解只要被正确使用，就会在生成的代码中表现为依赖项的标识符，从而在运行时进行正确的依赖注入。
 *
 * 总结
 * 虽然 @Dispatcher 是一个自定义的注解，但由于它使用了 @Qualifier，Dagger Hilt 能够在依赖注入过程中正确识别并解析它。Dagger Hilt 会根据 @Dispatcher(NiaDispatchers.Default) 和 @Dispatcher(NiaDispatchers.IO)
 * 这样的标识符来查找相应的依赖项，并将其注入到正确的地方。这种机制让开发者能够灵活地控制注入过程，尽管使用了自定义注解。
 *
 *
 */