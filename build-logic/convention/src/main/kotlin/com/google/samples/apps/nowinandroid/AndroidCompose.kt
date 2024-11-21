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

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Configure Compose-specific options
 * configureAndroidCompose: 定义一个内部函数，负责配置 Android 项目中的 Jetpack Compose 构建选项。它扩展 Project，意味着可以直接在 Project 对象上调用。
 *
 *  Project.configureAndroidCompose 是拓展函数的一种写法
 * configureAndroidCompose: 一个配置函数，启用 Compose 支持，并添加相关依赖、测试配置和编译器扩展。
 *
 */
internal fun Project.configureAndroidCompose(
    /**
     * commonExtension: CommonExtension<*, *, *, *, *, *>: 传入一个通用的 CommonExtension 对象，支持各种 Android 构建类型（如应用、库等）。
     */
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    /**
     * commonExtension.apply: 使用 apply 语法块，简化对 commonExtension 对象的多次调用。
     */
    commonExtension.apply {

        /**
         * buildFeatures { compose = true }: 启用 Jetpack Compose 支持，这是通过设置 buildFeatures.compose 为 true 来实现的。
         */
        buildFeatures {
            compose = true
        }

        /**
         * dependencies { }: 配置项目的依赖项。
         */
        dependencies {

            /**
             * libs.findLibrary("androidx-compose-bom").get(): 查找并获取 androidx-compose-bom 库，
             * 它是 Jetpack Compose 的 Bill of Materials (BOM)，用于统一管理 Compose 相关库的版本。
             */
            val bom = libs.findLibrary("androidx-compose-bom").get()

            /**
             * add(...): 将 Compose 相关依赖项添加到 implementation、androidTestImplementation 和 debugImplementation 配置中，确保 Compose UI 工具和预览功能在项目中可用。
             */
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        }

        /**
         * testOptions { unitTests { isIncludeAndroidResources = true } }: 配置单元测试选项，启用 Robolectric 测试框架对 Android 资源的支持。
         */
        testOptions {
            unitTests {
                // For Robolectric
                isIncludeAndroidResources = true
            }
        }
    }

    /**
     * extensions.configure<ComposeCompilerGradlePluginExtension> { }: 配置 Compose 编译器插件的扩展选项。
     */
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        /**
         * onlyIfTrue(): 定义一个扩展函数，作用于 Provider<String>，仅当提供的字符串转换为 true 时，继续执行后续操作。
         */
        fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }

        /**
         * relativeToRootProject(dir: String): 定义一个扩展函数，计算给定目录相对于根项目目录的位置。
         *
         */
        fun Provider<*>.relativeToRootProject(dir: String) = flatMap {
            rootProject.layout.buildDirectory.dir(projectDir.toRelativeString(rootDir))
        }.map { it.dir(dir) }

        /**
         * project.providers.gradleProperty("enableComposeCompilerMetrics"): 获取名为 enableComposeCompilerMetrics 的 Gradle 属性。
         * .onlyIfTrue(): 仅当属性值为 true 时继续执行。
         * .relativeToRootProject("compose-metrics"): 计算 "compose-metrics" 目录相对于根项目目录的位置。
         * .let(metricsDestination::set): 将计算后的路径设置为 metricsDestination，用于保存 Compose 编译器的指标数据。
         */

        project.providers.gradleProperty("enableComposeCompilerMetrics").onlyIfTrue()
            .relativeToRootProject("compose-metrics")
            .let(metricsDestination::set)

        /**
         * project.providers.gradleProperty("enableComposeCompilerReports"): 获取名为 enableComposeCompilerReports 的 Gradle 属性，用于控制是否生成 Compose 编译器报告。
         * .relativeToRootProject("compose-reports"): 计算 "compose-reports" 目录相对于根项目目录的位置。
         * .let(reportsDestination::set): 将计算后的路径设置为 reportsDestination，用于保存 Compose 编译器的报告数据。
         */
        project.providers.gradleProperty("enableComposeCompilerReports").onlyIfTrue()
            .relativeToRootProject("compose-reports")
            .let(reportsDestination::set)

        /**
         * stabilityConfigurationFile: 设置 Compose 编译器的稳定性配置文件路径。
         */
        stabilityConfigurationFile = rootProject.layout.projectDirectory.file("compose_compiler_config.conf")

        /**
         * enableStrongSkippingMode = true: 启用强跳过模式，可能用于优化编译器的性能。
         */
        enableStrongSkippingMode = true
    }
}
