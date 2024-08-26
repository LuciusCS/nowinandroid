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

package com.google.samples.apps.nowinandroid.core.network.di

import android.content.Context
import androidx.tracing.trace
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import com.google.samples.apps.nowinandroid.core.network.BuildConfig
import com.google.samples.apps.nowinandroid.core.network.demo.DemoAssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

/**
 * 这个代码定义了一个名为 NetworkModule 的 Dagger Hilt 模块，
 * 用于提供网络相关的依赖项，如 JSON 解析器、OkHttp 客户端和图像加载器。
 * 通过 @Module 和 @InstallIn(SingletonComponent::class) 注解，所有提供的依赖项将在整个应用程序生命周期内作为单例使用。
 *
 *
 * @InstallIn(SingletonComponent::class): 指定该模块中的依赖项将在整个应用程序生命周期内作为单例存在。
 * SingletonComponent 表示这些依赖项将在应用程序级别注入，并与应用程序的生命周期相同。
 * internal object NetworkModule: 定义了一个 NetworkModule 对象，该对象包含了一系列静态方法，用于提供依赖项。
 *
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    /**
     * @Provides: 标记该方法为 Dagger Hilt 提供的依赖项。
     * @Singleton: 指定返回的 Json 实例在应用程序中是单例的。
     * providesNetworkJson: 提供一个配置了 ignoreUnknownKeys 选项的 Json 实例，
     * 用于忽略 JSON 中未知的键。这个 Json 对象用于网络请求的 JSON 解析。
     *
     *
     * networkJson: Json 参数：Hilt 会在应用程序中查找已提供的 Json 实例。
     * 由于 NetworkModule 中定义了 providesNetworkJson 方法，Hilt 知道如何创建并提供 Json 实例
     * 。因此，Hilt 会自动将这个 Json 实例注入到 RetrofitNiaNetwork 的构造函数中。
     *
     */
    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    /**
     *
     * @Provides: 标记该方法为 Dagger Hilt 提供的依赖项。
     * @Singleton: 指定返回的 DemoAssetManager 实例在应用程序中是单例的。
     * @ApplicationContext context: Context:
     * 使用 @ApplicationContext 注解来请求应用程序的 Context，确保依赖项使用应用程序上下文而不是 Activity 或其他组件的上下文。
     * DemoAssetManager(context.assets::open): 返回一个 DemoAssetManager 实例，用于管理应用程序的资产（如文件）。
     * context.assets::open 是一个函数引用，用于打开资产文件。
     *
     */
    @Provides
    @Singleton
    fun providesDemoAssetManager(
        @ApplicationContext context: Context,
    ): /**
     *
     * context.assets::open: 这是一个 lambda 表达式，具体是对 AssetManager.open 方法的函数引用。
     * 它实现了 DemoAssetManager 的唯一方法 open(fileName: String): InputStream，用于打开应用程序的资产文件。
     */
        DemoAssetManager = DemoAssetManager(context.assets::open)

    /**
     * @Provides: 标记该方法为 Dagger Hilt 提供的依赖项。
     * @Singleton: 指定返回的 Call.Factory 实例在应用程序中是单例的。
     * trace("NiaOkHttpClient") { ... }: 使用 trace 函数为这段代码添加追踪，便于性能分析。
     * OkHttpClient.Builder(): 创建一个 OkHttpClient 的构建器，用于配置 OkHttpClient 实例。
     * addInterceptor(HttpLoggingInterceptor().apply { ... }): 添加一个日志拦截器，
     * 用于记录 HTTP 请求和响应的详细信息。只有在 BuildConfig.DEBUG 为 true 时，日志级别才会设置为 BODY，即记录所有请求和响应的细节。
     * build(): 构建并返回 OkHttpClient 实例，该实例将用于创建 HTTP 请求的 Call.Factory
     *
     */
    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory = trace("NiaOkHttpClient") {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            setLevel(HttpLoggingInterceptor.Level.BODY)
                        }
                    },
            )
            .build()
    }

    /**
     * Since we're displaying SVGs in the app, Coil needs an ImageLoader which supports this
     * format. During Coil's initialization it will call `applicationContext.newImageLoader()` to
     * obtain an ImageLoader.
     *
     * @see <a href="https://github.com/coil-kt/coil/blob/main/coil-singleton/src/main/java/coil/Coil.kt">Coil</a>
     *
     *
     * @Provides: 标记该方法为 Dagger Hilt 提供的依赖项。
     * @Singleton: 指定返回的 ImageLoader 实例在应用程序中是单例的。
     * okHttpCallFactory: dagger.Lazy<Call.Factory>: 使用 dagger.Lazy 包装 Call.Factory，以避免它被过早初始化。dagger.Lazy 是一种延迟加载机制，只有在真正需要时才会创建实例。
     * @ApplicationContext application: Context: 获取应用程序的 Context。
     * trace("NiaImageLoader") { ... }: 使用 trace 函数为这段代码添加追踪，便于性能分析。
     * ImageLoader.Builder(application): 创建一个 ImageLoader 构建器，用于配置 Coil 图像加载库的 ImageLoader 实例。
     * callFactory { okHttpCallFactory.get() }: 设置 ImageLoader 使用 OkHttpClient 作为其网络请求的 Call.Factory。okHttpCallFactory.get() 将延迟加载 OkHttpClient 实例。
     * components { add(SvgDecoder.Factory()) }: 添加 SvgDecoder 组件，使 ImageLoader 能够解码和加载 SVG 图像。
     * respectCacheHeaders(false): 配置 ImageLoader 忽略 HTTP 响应中的缓存头，强制重新加载图像。这在某些图像更新频繁但 URL 不变的情况下很有用。
     * apply { if (BuildConfig.DEBUG) { logger(DebugLogger()) } }: 如果处于调试模式，使用 DebugLogger 输出更详细的日志信息。
     * build(): 构建并返回配置好的 ImageLoader 实例。
     */
    @Provides
    @Singleton
    fun imageLoader(
        // We specifically request dagger.Lazy here, so that it's not instantiated from Dagger.
        okHttpCallFactory: dagger.Lazy<Call.Factory>,
        @ApplicationContext application: Context,
    ): ImageLoader = trace("NiaImageLoader") {
        ImageLoader.Builder(application)
            .callFactory { okHttpCallFactory.get() }
            .components { add(SvgDecoder.Factory()) }
            // Assume most content images are versioned urls
            // but some problematic images are fetching each time
            .respectCacheHeaders(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}
