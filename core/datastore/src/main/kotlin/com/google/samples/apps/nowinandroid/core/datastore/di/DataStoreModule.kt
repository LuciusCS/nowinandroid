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
 * 完整执行过程总结
 * 1、应用启动时：
 *     Hilt 初始化 SingletonComponent。
 *     DataStoreModule 加载，但 providesUserPreferencesDataStore 只有在首次需要时调用。
 * 2、首次访问 DataStore 时：
 *       创建 DataStore 实例。
 *        检查存储文件（如 user_preferences.pb），并尝试加载数据。
 * 3、每次访问存储文件时：
 *        如果文件不匹配或需要更新版本，则触发 migrations。
 *        如果文件已经是最新状态，则跳过迁移。
 * 4、迁移的触发频率：
 *        只在文件版本不匹配时触发，不是每次启动都执行。
 *       如果文件格式和版本始终匹配，则迁移不会执行。
 */

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

/**
 *
 * 在应用安装后，userPreferences.data 能够读取到数据的原因是因为 DataStore 在第一次访问时会自动加载存储在磁盘上的数据。如果这是应用的第一次运行，DataStore 会创建一个新的存储文件并使用默认值初始化它。
 *
 * DataStore 初始化及数据读取流程
 * 1、 安装时首次运行：
 *       当应用第一次安装并运行时，DataStore 会自动创建一个新的数据文件（例如 user_preferences.pb）。这个文件在首次访问时将是空的。
 *       在这种情况下，DataStore 会使用 默认值 来初始化数据。对于没有显式赋值的字段，默认值会被使用（例如 0、false 或空列表等）。
 * 2、 首次读取时的行为：
 *        当你调用 userPreferences.data（例如通过 collect 或 first 等操作），DataStore 会读取 user_preferences.pb 文件中的内容。
 *        如果是第一次访问并且文件为空，DataStore 会使用你在 UserPreferences protobuf 文件中定义的默认值初始化所有字段。
 * 3、 为什么能读取到数据：
 *        默认值初始化：即使应用第一次运行，DataStore 也会通过默认值初始化所有字段，而不是返回 null 或引发错误。例如，int32 字段的默认值是 0，布尔字段的默认值是 false，重复字段的默认值是空列表。
 *        序列化和反序列化：DataStore 会根据定义的 UserPreferences 类对数据进行序列化和反序列化处理。即使没有数据存储在文件中，它也会为每个字段提供默认值。这样，即使没有任何数据，应用仍然可以正常工作。
 * 4、 后续访问：
 *     如果数据已存储在磁盘上（如应用已使用一段时间并写入了 user_preferences.pb 文件），DataStore 会加载已存储的数据。如果数据没有被修改，返回的将是上次存储的数据；如果有更新，DataStore 会在读取时自动反序列化并提供最新数据。
 *
 *
 *
 * 详细说明：
 * 1、 DataStore 的工作机制：
 *        DataStore 是基于存储的文件系统（如磁盘）来持久化数据的。如果在应用第一次启动时文件不存在，它会创建一个新文件并写入数据（如果没有显式写入数据，它会使用默认值）。
 *        userPreferences.data 是通过 Flow 来观察的数据，当你调用它时，DataStore 会触发对文件的读取，解析成对应的数据结构（例如 UserPreferences），并返回给调用者。
 * 2、默认值的作用：
 * proto3 中的字段有预设的默认值。例如，int32 类型字段的默认值是 0，布尔类型的默认值是 false，字符串类型的默认值是空字符串，等等。通过这些默认值，即使没有显式的存储数据，应用仍然可以读取到这些默认值。
 *
 *
 * 总结：
 * 当应用首次安装并运行时，DataStore 会自动初始化存储文件，并根据 UserPreferences protobuf 的定义为每个字段赋予默认值。即使没有任何用户数据，读取 userPreferences.data 时会返回这些默认值，因此应用在启动时能正常读取数据。
 */