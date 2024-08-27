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

import com.android.build.gradle.LibraryExtension
import com.google.samples.apps.nowinandroid.configureGradleManagedDevices
import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("nowinandroid.android.library")
                apply("nowinandroid.hilt")
            }
            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true
                configureGradleManagedDevices(this)
            }

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:designsystem"))

                add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewModelCompose").get())
                add("implementation", libs.findLibrary("androidx.tracing.ktx").get())

                add("androidTestImplementation", libs.findLibrary("androidx.lifecycle.runtimeTesting").get())
            }
        }
    }
}

/***
 *
 *
 *
 * 当两个 ConventionPlugin 都包含相同的配置时，可能会引发冲突，导致配置被覆盖或出现意外行为。在处理这种冲突时，可以采用以下几种方法来避免或解决这些问题：
 *
 * 1. 明确配置顺序
 * Gradle 配置顺序: Gradle 是顺序配置的，因此后应用的插件会覆盖之前的配置。如果两个插件都配置了相同的属性或任务，最后一个应用的插件的配置将生效。
 * 解决方法: 确保按预期的顺序应用插件。你可以控制插件的应用顺序，以决定哪个插件的配置优先级更高。
 * kotlin
 * Copy code
 * apply(plugin = "first.convention.plugin")
 * apply(plugin = "second.convention.plugin")
 * 2. 使用条件检查
 * 条件配置: 在每个插件中使用条件检查，只有在配置尚未被设置时才进行配置。这可以避免重复配置同一属性。
 * kotlin
 * Copy code
 * extensions.configure<ApplicationExtension> {
 *     if (defaultConfig.targetSdk == null) {
 *         defaultConfig.targetSdk = 34
 *     }
 * }
 * 解决方法: 在插件中使用 if 条件判断，确保只在必要时进行配置，避免无意的覆盖。
 * 3. 合并配置
 * 配置合并: 如果两个插件都需要对相同的属性或任务进行配置，可以尝试将配置进行合并。例如，如果两个插件都要添加编译选项，可以将它们合并到同一个列表中。
 * kotlin
 * Copy code
 * extensions.configure<ApplicationExtension> {
 *     defaultConfig {
 *         targetSdk = 34
 *         compileOptions {
 *             sourceCompatibility = JavaVersion.VERSION_1_8
 *             targetCompatibility = JavaVersion.VERSION_1_8
 *         }
 *     }
 * }
 * 解决方法: 在插件实现中，通过合并操作将多个插件的配置整合在一起，而不是简单地覆盖。
 * 4. 使用自定义扩展点
 * 自定义扩展点: 提供自定义扩展点（如接口或抽象类），允许其他插件实现这些扩展点，以便在配置时进行协调。这种方法适合复杂项目，尤其是在多个插件可能相互依赖的情况下。
 * kotlin
 * Copy code
 * interface CustomPluginExtension {
 *     fun applyCustomConfig(extension: ApplicationExtension)
 * }
 * 解决方法: 让每个插件实现 CustomPluginExtension 接口，在合适的时机调用它们以进行配置协调。
 * 5. 在插件之间进行通信
 * 插件间通信: 允许插件之间进行通信，明确哪些插件会影响相同的配置，并协调配置的应用。你可以使用共享的扩展或属性，或者在插件中显式调用另一个插件的配置方法。
 * kotlin
 * Copy code
 * plugins.withId("first.convention.plugin") {
 *     // 配置与 "first.convention.plugin" 协调
 * }
 * 解决方法: 在插件中使用 plugins.withId 或类似的方法来检测其他插件的存在，并在配置时做出相应的调整。
 * 6. 文档化和沟通
 * 文档化: 清楚地记录每个插件的配置行为，特别是哪些属性或任务可能会与其他插件发生冲突。为使用这些插件的开发者提供清晰的指导。
 * 解决方法: 提供详细的文档，说明如何组合使用多个插件，并告知可能的冲突和解决方案。
 * 总结
 * 配置顺序和条件检查是最常用的简单方法。
 * 合并配置和自定义扩展点适用于需要更精细控制的场景。
 * 插件间通信适用于需要高度协调的复杂项目。
 * 通过这些方法，可以有效处理多个 ConventionPlugin 之间的配置冲突，确保项目的配置按预期工作。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

