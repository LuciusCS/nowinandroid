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

import com.google.samples.apps.nowinandroid.core.network.demo.DemoAssetManager
import java.io.File
import java.io.InputStream
import java.util.Properties

/**
 * This class helps with loading Android `/assets` files, especially when running JVM unit tests.
 * It must remain on the root package for an easier [Class.getResource] with relative paths.
 * @see <a href="https://developer.android.com/reference/tools/gradle-api/7.3/com/android/build/api/dsl/UnitTestOptions">UnitTestOptions</a>
 *
 *
 * JvmUnitTestDemoAssetManager: 用于在 JVM 环境下（如单元测试中）模拟 DemoAssetManager。
 * config: 通过 javaClass.getResource 获取测试配置文件的资源路径。如果资源文件不存在，抛出异常并给出提示信息。
 * properties: 读取配置文件中的属性，用于获取合并后的资源目录路径。
 * assets: 表示资源目录的 File 对象。
 * open(fileName: String): InputStream: 使用文件路径创建输入流，模拟从资产目录中打开文件。
 *
 *
 */

internal object JvmUnitTestDemoAssetManager : DemoAssetManager {
    private val config =
        requireNotNull(javaClass.getResource("com/android/tools/test_config.properties")) {
            """
            Missing Android resources properties file.
            Did you forget to enable the feature in the gradle build file?
            android.testOptions.unitTests.isIncludeAndroidResources = true
            """.trimIndent()
        }
    private val properties = Properties().apply { config.openStream().use(::load) }
    private val assets = File(properties["android_merged_assets"].toString())

    /***
     * open(fileName: String): InputStream: 使用文件路径创建输入流，模拟从资产目录中打开文件。
     *
     */
    override fun open(fileName: String): InputStream = File(assets, fileName).inputStream()
}
