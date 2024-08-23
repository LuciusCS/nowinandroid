/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.android.build.api.dsl.ApplicationExtension
import com.google.samples.apps.nowinandroid.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

/**
 *
 * AndroidApplicationComposeConventionPlugin: 这是一个自定义 Gradle 插件类，用于为 Android 项目设置特定的 Compose 配置。
 * Plugin<Project>: 该类实现了 Plugin<Project> 接口，表示它是一个可以应用于 Project 的 Gradle 插件。
 *
 * AndroidApplicationComposeConventionPlugin: 一个自定义 Gradle 插件类，用于自动配置 Android 项目中的 Jetpack Compose 构建选项。
 */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {

    /**
     * apply(target: Project): 重写 apply 方法，该方法在插件应用到项目时被 Gradle 调用。target 是指代当前项目的 Project 对象。
     *
     *
     */
    override fun apply(target: Project) {
        /**
         *
         * with(target): 使用 with 语法块，简化对 target 对象的多次调用。
         */
        with(target) {
            /**
             * apply(plugin = "com.android.application"): 应用 Android 应用程序插件，使得这个项目具备 Android 应用程序的构建能力。
             */
            apply(plugin = "com.android.application")
            /**
             * apply(plugin = "org.jetbrains.kotlin.plugin.compose"): 应用 JetBrains Kotlin 的 Compose 插件，使得该项目支持 Kotlin 的 Jetpack Compose 编译功能。
             */
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")

            /**
             * extensions.getByType<ApplicationExtension>(): 获取当前项目的 ApplicationExtension 扩展对象，用于访问和配置 Android 特定的构建选项。
             */
            val extension = extensions.getByType<ApplicationExtension>()
            /**
             * configureAndroidCompose(extension): 调用 configureAndroidCompose 函数，将 ApplicationExtension 对象传递给它，用于配置 Compose 相关的构建选项。
             */
            configureAndroidCompose(extension)
        }
    }

}
