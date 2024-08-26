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

package com.google.samples.apps.nowinandroid.core.network.retrofit

import androidx.tracing.trace
import com.google.samples.apps.nowinandroid.core.network.BuildConfig
import com.google.samples.apps.nowinandroid.core.network.NiaNetworkDataSource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkChangeList
import com.google.samples.apps.nowinandroid.core.network.model.NetworkNewsResource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkTopic
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit API declaration for NIA Network API
 *
 * 定义了一个私有接口 RetrofitNiaNetworkApi，它用于声明与网络相关的 API 接口方法，这些方法会在后续的 Retrofit 配置中实现。
 */
private interface RetrofitNiaNetworkApi {

    /**
     * @GET(value = "topics")
     * 解释: 这是 Retrofit 的注解，用于声明一个 GET 请求，value 指定了请求的路径部分（相对于 baseUrl）。
     *
     * 定义了一个 suspend 函数 getTopics，用于异步获取主题数据
     */
    @GET(value = "topics")
    suspend fun getTopics(
        /**
         * @Query("id"): 将 ids 作为查询参数附加到 URL 中。
         */
        @Query("id") ids: List<String>?,
    ):
        /**
         * 返回类型是 NetworkResponse<List<NetworkTopic>>，表示服务器返回的数据被封装在 NetworkResponse 对象中
         */

        NetworkResponse<List<NetworkTopic>>

    @GET(value = "newsresources")
    suspend fun getNewsResources(
        @Query("id") ids: List<String>?,
    ): NetworkResponse<List<NetworkNewsResource>>

    @GET(value = "changelists/topics")
    suspend fun getTopicChangeList(
        @Query("after") after: Int?,
    ): List<NetworkChangeList>

    @GET(value = "changelists/newsresources")
    suspend fun getNewsResourcesChangeList(
        @Query("after") after: Int?,
    ): List<NetworkChangeList>
}

/**
 * 解释: 定义了一个私有常量 NIA_BASE_URL，其值来自 BuildConfig.BACKEND_URL。这是 Retrofit 使用的基 URL（base URL）
 * ，通常是在 build.gradle 文件中通过 buildConfigField 配置的。
 */
private const val NIA_BASE_URL = BuildConfig.BACKEND_URL

/**
 * Wrapper for data provided from the [NIA_BASE_URL]
 *
 * 这是一个 Kotlin 序列化注解，用于标记数据类 NetworkResponse 是可序列化的，这样它可以与 JSON 进行相互转换。
 * : 定义了一个私有数据类 NetworkResponse，它是一个泛型类，用于封装网络响应的数据。data 是实际的响应数据，类型为 T。
 */
@Serializable
private data class NetworkResponse<T>(
    val data: T,
)

/**
 * [Retrofit] backed [NiaNetworkDataSource]
 *
 * : @Singleton 是一个 Dagger 注解，表示 RetrofitNiaNetwork 类的实例在整个应用生命周期中是单例的，即只会创建一个实例。
 *
 *  定义了 RetrofitNiaNetwork 类，并通过构造函数注入了所需的依赖项。internal 修饰符表示该类在模块内可见。
 */
@Singleton
internal class RetrofitNiaNetwork @Inject constructor(
    /**
     * 构造函数参数 networkJson 是一个 Json 对象，用于处理 JSON 数据的序列化和反序列化。
     */
    networkJson: Json,
    /**
     * 构造函数参数 okhttpCallFactory 是一个 dagger.Lazy<Call.Factory> 对象，
     * 表示对 OkHttp 的 Call.Factory 的延迟加载。这可以防止在主线程上初始化 OkHttp，以提高性能。
     */
    okhttpCallFactory: dagger.Lazy<Call.Factory>,

    /**
     * RetrofitNiaNetwork 类实现了 NiaNetworkDataSource 接口，提供了对网络数据源的访问。
     */
) : NiaNetworkDataSource {

    /**
     * : networkApi 是 RetrofitNiaNetworkApi 接口的一个实例，通过
     * trace 代码块创建，用于跟踪性能或调试。trace 是一个自定义的函数或工具，用于记录运行时的某些信息。
     */
    private val networkApi = trace("RetrofitNiaNetwork") {
        Retrofit.Builder()
            .baseUrl(NIA_BASE_URL)
            // We use callFactory lambda here with dagger.Lazy<Call.Factory>
            // to prevent initializing OkHttp on the main thread.
            /**
             * 设置 Retrofit 的 Call.Factory，使用了延迟加载的 okhttpCallFactory，确保 OkHttp 的实例不会在主线程上初始化。
             */
            .callFactory { okhttpCallFactory.get().newCall(it) }
            /**
             *  添加一个 JSON 转换器工厂，用于将 JSON 数据转换为 Kotlin 对象。这是通过 networkJson 对象（通常是 Kotlinx 序列化库的 Json 实例）实现的。
             */
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            /**
             * 使用 Retrofit 实例创建 RetrofitNiaNetworkApi 接口的实现，这样就可以调用接口中的方法来进行网络请求。
             */
            .create(RetrofitNiaNetworkApi::class.java)
    }

    override suspend fun getTopics(ids: List<String>?): List<NetworkTopic> =
        networkApi.getTopics(ids = ids).data

    override suspend fun getNewsResources(ids: List<String>?): List<NetworkNewsResource> =
        networkApi.getNewsResources(ids = ids).data

    override suspend fun getTopicChangeList(after: Int?): List<NetworkChangeList> =
        networkApi.getTopicChangeList(after = after)

    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
        networkApi.getNewsResourcesChangeList(after = after)
}
