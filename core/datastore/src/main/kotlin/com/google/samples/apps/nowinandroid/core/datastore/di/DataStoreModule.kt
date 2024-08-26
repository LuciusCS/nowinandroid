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

package com.google.samples.apps.nowinandroid.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.google.samples.apps.nowinandroid.core.datastore.IntToStringIdsMigration
import com.google.samples.apps.nowinandroid.core.datastore.UserPreferences
import com.google.samples.apps.nowinandroid.core.datastore.UserPreferencesSerializer
import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.IO
import com.google.samples.apps.nowinandroid.core.network.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 *
 * 使用 Hilt 提供依赖注入的 Kotlin 模块，主要用于创建和提供一个 DataStore 对象。
 * DataStore 是 Android Jetpack 中用于存储数据的组件，类似于 SharedPreferences，但提供了更好的类型安全性和异步处理能力。
 *
 *
 * object DataStoreModule：定义了一个 object 单例，表示这是一个静态对象，所有的方法和属性都在类加载时初始化。
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * @Provides：这个注解表明该方法将提供一个依赖项（DataStore<UserPreferences>），并告诉 Hilt 如何创建它。
     * @Singleton：这个注解表示该方法提供的依赖项在整个应用生命周期中是单例的。
     * internal fun providesUserPreferencesDataStore(...)：定义了一个内部函数，用于提供 DataStore<UserPreferences> 对象。
     */
    @Provides
    @Singleton
    internal fun providesUserPreferencesDataStore(
        /**
         * @ApplicationContext context: Context：通过 Hilt 注入应用程序上下文（Context），用来访问文件系统。
         */
        @ApplicationContext context: Context,
        @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
        /**
         * @ApplicationScope scope: CoroutineScope：通过 Hilt 注入应用程序级别的 CoroutineScope，用于定义协程的生命周期范围。
         */
        @ApplicationScope scope: CoroutineScope,
        /**
         * userPreferencesSerializer: UserPreferencesSerializer：
         * 注入一个 UserPreferencesSerializer，用于序列化和反序列化 UserPreferences 对象。
         */
        userPreferencesSerializer: UserPreferencesSerializer,
    ): DataStore<UserPreferences> =

        /**
         * DataStoreFactory.create(...)：使用 DataStoreFactory 创建 DataStore 实例。
         */

        DataStoreFactory.create(
            /**
             * serializer = userPreferencesSerializer：指定序列化器，用于将 UserPreferences 对象转换为字节流并保存到文件中，
             * 或者从字节流恢复到 UserPreferences 对象。
             */
            serializer = userPreferencesSerializer,
            /**
             * scope = CoroutineScope(scope.coroutineContext + ioDispatcher)：为 DataStore 指定协程作用域，
             * 该作用域是应用程序范围的 CoroutineScope 与 IO 调度器的结合。DataStore 的操作将在这个协程范围内运行。
             */
            scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
            /**
             * migrations = listOf(IntToStringIdsMigration)：指定 DataStore 的迁移列表。
             * IntToStringIdsMigration 是一个迁移策略，用于在数据结构变化时处理旧数据的转换。
             */
            migrations = listOf(
                IntToStringIdsMigration,
            ),
        ) {
            /**
             * 指定 DataStore 使用的文件路径和名称。在这里，
             * 数据将保存在 user_preferences.pb 文件中。.pb 通常表示 Protobuf（Protocol Buffers）格式。
             */
            context.dataStoreFile("user_preferences.pb")
        }
}
