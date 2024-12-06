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

package com.google.samples.apps.nowinandroid.core.data.di

import com.google.samples.apps.nowinandroid.core.data.repository.DefaultRecentSearchRepository
import com.google.samples.apps.nowinandroid.core.data.repository.DefaultSearchContentsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.NewsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.OfflineFirstNewsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.OfflineFirstTopicsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.OfflineFirstUserDataRepository
import com.google.samples.apps.nowinandroid.core.data.repository.RecentSearchRepository
import com.google.samples.apps.nowinandroid.core.data.repository.SearchContentsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.TopicsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.data.util.ConnectivityManagerNetworkMonitor
import com.google.samples.apps.nowinandroid.core.data.util.NetworkMonitor
import com.google.samples.apps.nowinandroid.core.data.util.TimeZoneBroadcastMonitor
import com.google.samples.apps.nowinandroid.core.data.util.TimeZoneMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 *
 * @Module 注解用于标记这个类为一个 Dagger 模块。模块是依赖注入的容器，Dagger 会从这些模块中找到用于提供依赖的方法。
 *
 * @InstallIn 注解指定了模块将会在 Dagger Hilt 的哪个组件（Component）中进行安装。这个组件定义了这个模块提供的依赖的生命周期。
 *
 * SingletonComponent 是 Hilt 中的一个组件，它代表应用级别的生命周期，意味着该模块中的依赖会在应用生命周期内共享，通常用来提供单例服务。
 * 这个注解的作用是告诉 Hilt 要在 SingletonComponent 中“安装”这个模块（DataModule）。模块中的依赖（通过 @Binds 或 @Provides 提供的依赖）
 * 将被 Dagger Hilt 注入到需要它们的地方，并且这些依赖会在应用的整个生命周期内共享。
 *
 *
 *
 */


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /**
     * @Binds 注解用于告诉 Dagger Hilt 如何将接口 TopicsRepository 绑定到具体的实现 OfflineFirstTopicsRepository 上。
     * 也就是说，当需要 TopicsRepository 依赖时，Dagger 会提供 OfflineFirstTopicsRepository 的实例。
     * bindsTopicRepository 是一个抽象函数，它将 OfflineFirstTopicsRepository 类型的实例绑定到 TopicsRepository 接口上。
     * Hilt 会自动识别并提供 OfflineFirstTopicsRepository 类型的实例，在应用中注入到 TopicsRepository 类型需要的地方。
     */
    @Binds
    internal abstract fun bindsTopicRepository(
        topicsRepository: OfflineFirstTopicsRepository,
    ): TopicsRepository

    @Binds
    internal abstract fun bindsNewsResourceRepository(
        newsRepository: OfflineFirstNewsRepository,
    ): NewsRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineFirstUserDataRepository,
    ): UserDataRepository

    @Binds
    internal abstract fun bindsRecentSearchRepository(
        recentSearchRepository: DefaultRecentSearchRepository,
    ): RecentSearchRepository

    @Binds
    internal abstract fun bindsSearchContentsRepository(
        searchContentsRepository: DefaultSearchContentsRepository,
    ): SearchContentsRepository

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor

    @Binds
    internal abstract fun binds(impl: TimeZoneBroadcastMonitor): TimeZoneMonitor
}
