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

package com.google.samples.apps.nowinandroid.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * An [androidx.datastore.core.Serializer] for the [UserPreferences] proto.
 *
 * 定义了一个 UserPreferencesSerializer 类，用于序列化和反序列化 UserPreferences 对象，
 * 以便存储在 DataStore 中。它实现了 Serializer<UserPreferences> 接口，提供了 UserPreferences 对象的默认值，
 * 以及如何将其读入和写出到 DataStore 所使用的输入/输出流中。
 *
 * class UserPreferencesSerializer：定义了一个名为 UserPreferencesSerializer 的类，用于处理 UserPreferences 的序列化和反序列化。
 *
 *
 * @Inject constructor()：使用 Dagger 或 Hilt 的 @Inject 注解标记构造函数，允许依赖注入框架自动创建 UserPreferencesSerializer 实例。
 * 这意味着 UserPreferencesSerializer 可以作为依赖项被注入到需要它的地方。
 *
 * : Serializer<UserPreferences>：UserPreferencesSerializer
 * 实现了 Serializer<UserPreferences> 接口，该接口定义了序列化和反序列化的方法。
 *
 *
 * UserPreferences 是在 datastore-proto中定义，并编译生成的
 *
 */
class UserPreferencesSerializer @Inject constructor() : Serializer<UserPreferences> {

    /**
     * override val defaultValue: UserPreferences：重写 Serializer 接口中的 defaultValue 属性，指定 UserPreferences 的默认实例。
     * UserPreferences.getDefaultInstance()：调用 UserPreferences 类的静态方法，获取其默认实例。
     * 这个默认实例通常表示一个初始化的、空的或零值的 UserPreferences 对象。
     *
     */
    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()

    /**
     * override suspend fun readFrom(input: InputStream): UserPreferences：重写 Serializer 接口中的 readFrom 方法，
     * 该方法用于从 InputStream 中读取数据并将其反序列化为 UserPreferences 对象。
     */
    override suspend fun readFrom(input: InputStream): UserPreferences =
        try {
            // readFrom is already called on the data store background thread
            //input: InputStream：这是从 DataStore 文件中读取的数据流。
            /**
             * UserPreferences.parseFrom(input)：使用 UserPreferences 的 parseFrom 方法，从 input 流中读取数据，
             * 并将其解析为 UserPreferences 对象。parseFrom 是 Protocol Buffers 生成的方法，用于反序列化数据。
             */
            UserPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    /**
     * override suspend fun writeTo(t: UserPreferences, output: OutputStream)：
     * 重写 Serializer 接口中的 writeTo 方法，该方法用于将 UserPreferences 对象序列化并写入 OutputStream 中。
     * output: OutputStream：数据将被写入的输出流，通常指向 DataStore 的文件。
     */
    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}
