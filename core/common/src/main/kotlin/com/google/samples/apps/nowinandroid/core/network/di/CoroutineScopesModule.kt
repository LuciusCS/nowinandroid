/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.core.network.di

import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.Default
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

/**
 * @Module 这是一个Dagger Hilt的注解，用于标记一个类或对象是一个依赖注入模块。模块包含提供依赖项的方法，这些方法可以被其他地方使用。
 *
 * @InstallIn(SingletonComponent::class) 这个注解指定了这个模块的生命周期范围。在这里，它被安装在SingletonComponent中，这意味着其中提供的依赖项将在应用程序的整个生命周期中都是单例的。
 *
 * : CoroutineScopesModule是一个对象（即单例），它包含提供协程作用域的依赖注入方法。internal表示该对象在模块内可见，无法在模块外部访问。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CoroutineScopesModule {
    /**
     * @Provides注解用于标记一个方法，该方法将提供一个依赖项的实例。Dagger Hilt将使用这个方法创建并注入所需的依赖项。
     *  @Singleton注解指定这个提供的方法返回的对象是一个单例对象，即在应用程序的整个生命周期中只会创建一次这个对象。
     *
     *  这是一个自定义的作用域注解，通常用于指定依赖项在应用程序级别的作用域。这意味着在应用程序的整个生命周期中，该作用域内的对象是同一个实例。
     *
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun providesCoroutineScope(
        /**
         * @Dispatcher(Default) dispatcher: CoroutineDispatcher: 通过注入的方式，
         * 提供了一个默认的CoroutineDispatcher。这个注解@Dispatcher(Default)通常用于标记该方法需要特定的调度器（如默认调度器）。
         */
        @Dispatcher(Default) dispatcher: CoroutineDispatcher,
    ): /**
    CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher): 该方法返回一个CoroutineScope对象，
      使用SupervisorJob和传入的dispatcher创建。SupervisorJob允许其子协程独立于其他子协程的失败而执行。
     */

        CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}

/**
 *
 * SupervisorJob 是 Kotlin 中协程的 Job 的一种，它在结构化并发中扮演着重要的角色。SupervisorJob 的作用是管理其子协程的生命周期，允许其子协程独立运行，即使一个子协程失败，也不会影响其他子协程的运行。让我们详细看一下 SupervisorJob 的作用和使用场景。
 *
 * 作用
 * 独立的子协程处理:
 *
 * SupervisorJob 确保了在其作用域内的所有子协程是独立的。如果某个子协程由于异常而失败，其他子协程不会因此被取消。这与普通的 Job 不同，普通的 Job 会取消整个作用域中的所有协程。
 * 适用于需要容忍部分失败的场景:
 *
 * 在某些场景下，任务的执行可能会分成多个子任务，即多个子协程。如果其中一个子任务失败，使用 SupervisorJob 可以保证其他子任务仍然能够继续执行。
 * 使用场景
 * SupervisorJob 适用于那些希望在协程失败时，其他协程能够继续执行的情况。以下是一些常见的使用场景：
 *
 * 并行处理多个独立任务:
 *
 * 比如，你可能有多个网络请求或数据库操作需要并行执行。如果某个请求失败，你希望其他请求继续运行并完成，这时候 SupervisorJob 就非常合适。
 * ViewModel 中的协程:
 *
 * 在 Android 的 ViewModel 中使用协程时，可以使用 SupervisorJob 来保证即使某个协程因为某种原因失败，其他协程依然可以继续工作，避免整个 ViewModel 的任务都被取消。
 * 使用示例
 * kotlin
 * Copy code
 * import kotlinx.coroutines.*
 *
 * fun main() = runBlocking {
 *     val supervisor = SupervisorJob()
 *
 *     // 创建一个 SupervisorScope，这里每个子协程都将独立运行
 *     with(CoroutineScope(coroutineContext + supervisor)) {
 *         // 启动第一个子协程
 *         val firstChild = launch {
 *             println("First child is running")
 *             delay(1000)
 *             println("First child completed")
 *         }
 *
 *         // 启动第二个子协程，并使其抛出异常
 *         val secondChild = launch {
 *             println("Second child is running")
 *             delay(500)
 *             throw Exception("Second child failed")
 *         }
 *
 *         // 等待所有的子协程完成
 *         firstChild.join()
 *         secondChild.join()
 *     }
 *
 *     println("Supervisor job completed")
 * }
 * 解释
 * 在这个例子中，firstChild 和 secondChild 是 SupervisorJob 下的两个子协程。
 * secondChild 抛出异常并失败，但由于使用了 SupervisorJob，firstChild 并不会因此被取消，它可以继续执行并完成。
 * 总结
 * SupervisorJob 是一个非常有用的工具，特别是在你希望容忍部分子协程失败的场景下。它提供了一种更为灵活的协程管理方式，使得某些子协程的失败不会影响整个作用域中的其他协程。
 */
