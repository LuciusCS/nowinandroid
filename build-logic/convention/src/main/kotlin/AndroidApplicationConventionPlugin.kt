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
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.google.samples.apps.nowinandroid.configureBadgingTasks
import com.google.samples.apps.nowinandroid.configureGradleManagedDevices
import com.google.samples.apps.nowinandroid.configureKotlinAndroid
import com.google.samples.apps.nowinandroid.configurePrintApksTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        /**
         * with(target):
         * 目的: 对传入的 target 对象（即 Project 对象）执行多项操作。
         * 好处: 使得后续代码可以直接调用 target 的方法和属性，而不需要重复写 target.。
         * with(pluginManager):
         * 目的: 在 target 对象的上下文中，针对其 pluginManager 执行多个操作。
         * 好处: 避免多次写 pluginManager.，简化了插件应用的代码。
         */
        with(target) {

            /**
             * 插件管理: pluginManager 是 Project 对象的一部分，负责管理所有应用到该项目的插件。它提供了以下几种主要功能：
             * 应用插件: 通过 apply() 方法可以应用插件，不管是内置的 Gradle 插件，还是自定义的插件。
             * 检查插件: 可以检查某个插件是否已经应用过，或者插件的应用状态。
             * 插件事件监听: 你可以监听插件应用的事件，并在插件应用时执行额外的逻辑。
             */
            with(pluginManager) {

                /**
                 * target.pluginManager.apply("com.android.application") 是用于将 Android 应用程序插件应用到当前项目。
                 * 应用该插件后，项目将具备构建 Android 应用程序所需的任务和配置。
                 * pluginManager 的主要作用是管理插件的应用过程，并在项目中管理这些插件的生命周期和交互。
                 *
                 *
                 * 该插件会添加许多任务（如编译、打包、Lint 检查等）和扩展（如 android 扩展）到你的项目中。
                 *
                 *
                 * 1. 应用 Android 插件:
                 * Gradle 将通过插件 ID "com.android.application" 找到并加载对应的 Android 应用程序插件。
                 * 该插件会添加许多任务（如编译、打包、Lint 检查等）和扩展（如 android 扩展）到你的项目中。
                 * 2. 配置项目结构:
                 * Android 插件将定义和配置 Android 应用程序特有的构建逻辑，比如构建变体、签名配置、多模块支持等。
                 * 3. 影响 Gradle 构建生命周期:
                 * 一旦插件被应用，它会在项目的生命周期中添加必要的钩子和配置，使得你的项目能够正确构建一个 Android 应用程序。
                 *
                 *
                 */

                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("nowinandroid.android.lint")
                apply("com.dropbox.dependency-guard")
            }

            /**
             * extensions.configure<ApplicationExtension>: 这是在 target 的扩展（extensions）中配置 ApplicationExtension，
             * 这是由 Android 应用插件（com.android.application）提供的一个扩展。这个扩展用于配置 Android 应用的构建选项，如编译 SDK 版本、应用签名、打包选项等。
             */
            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
                /**
                 * 这是一个注解，用于抑制编译器关于不稳定 API 使用的警告。testOptions.animationsDisabled = true 可能依赖于不稳定的 API。
                 */
                @Suppress("UnstableApiUsage")
                testOptions.animationsDisabled = true  //配置测试选项，使测试运行时禁用动画。这有助于在测试中减少非确定性行为，提高测试的稳定性和一致性。
                /**
                 * configureGradleManagedDevices(this): 调用另一个自定义方法 configureGradleManagedDevices，
                 * 用于配置 Gradle 管理的设备。这通常涉及到设置模拟器或物理设备的配置，以便在构建过程中进行自动化测试。
                 */
                configureGradleManagedDevices(this)
            }

            /**
             *  这是在 target 的扩展（extensions）中配置 ApplicationAndroidComponentsExtension，
             *  这是 Android Gradle Plugin 7.x 引入的新扩展，用于配置与应用程序组件（如活动、服务、广播接收器等）相关的构建选项和任务。
             *
             *
             *  extensions: 这是 Project 的一个属性，它管理和提供对所有已添加到项目中的扩展（extensions）的访问。这些扩展可以通过插件添加，或者是 Gradle 本身提供的。
             * configure<T>: 这是一个用于配置扩展的函数。它的作用是接收某种类型的扩展（在这里是 ApplicationAndroidComponentsExtension），然后对其进行配置。
             * <ApplicationAndroidComponentsExtension>: 这是指定要配置的扩展的类型。在 Android 项目中，ApplicationAndroidComponentsExtension 是由 Android Gradle Plugin 提供的一个扩展，用于管理和配置 Android 应用组件（如活动、服务等）的相关设置和任务。
             * 作用与效果
             * 查找并配置现有扩展: extensions.configure<ApplicationAndroidComponentsExtension> 通过 extensions 查找当前项目中已存在的 ApplicationAndroidComponentsExtension 实例，然后进入该实例的上下文进行配置。
             * 追加或修改配置: 在 configure 的 lambda 块中，开发者可以追加或修改 ApplicationAndroidComponentsExtension 的现有配置。比如添加自定义任务、修改已有任务的行为、设置额外的编译选项等。
             *
             *
             *
             */
            extensions.configure<ApplicationAndroidComponentsExtension> {
                /**
                 * configurePrintApksTask(this): 调用一个自定义方法 configurePrintApksTask，
                 * 传递当前的 ApplicationAndroidComponentsExtension 实例 (this)。该方法可能用于在构建完成后自动打印生成的 APK 文件信息。
                 */
                configurePrintApksTask(this)
                /**
                 * configureBadgingTasks(extensions.getByType<BaseExtension>(), this): 通过 extensions.getByType<BaseExtension>() 获取项目中的 BaseExtension 实例，并将其与当前的 ApplicationAndroidComponentsExtension
                 * 一起传递给 configureBadgingTasks 方法。configureBadgingTasks 可能用于设置应用徽章任务，例如为应用程序生成带有版本信息的图标。
                 */
                configureBadgingTasks(extensions.getByType<BaseExtension>(), this)
            }
        }
    }

}

/**
 *
 * 如果不使用with函数，应该按照以下的写法
 *
 * class AndroidApplicationConventionPlugin : Plugin<Project> {
 *     override fun apply(target: Project) {
 *         // 必须显式地引用 target 对象
 *         target.pluginManager.apply("com.android.application")
 *         target.pluginManager.apply("org.jetbrains.kotlin.android")
 *         target.pluginManager.apply("nowinandroid.android.lint")
 *         target.pluginManager.apply("com.dropbox.dependency-guard")
 *
 *         // 必须显式地引用 target 对象来配置 extensions
 *         target.extensions.configure<ApplicationExtension> {
 *             configureKotlinAndroid(this)
 *             defaultConfig.targetSdk = 34
 *             @Suppress("UnstableApiUsage")
 *             testOptions.animationsDisabled = true
 *             configureGradleManagedDevices(this)
 *         }
 *
 *         // 再次显式地引用 target 对象来配置 extensions
 *         target.extensions.configure<ApplicationAndroidComponentsExtension> {
 *             configurePrintApksTask(this)
 *             configureBadgingTasks(target.extensions.getByType<BaseExtension>(), this)
 *         }
 *     }
 * }
 *
 *
 *
 *
 *
 *
 *
 *
 */