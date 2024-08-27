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
import com.google.samples.apps.nowinandroid.NiaBuildType

/**
 *
 *
 * 在 Gradle 构建脚本中，plugins 块用于声明和应用插件。你提到的三种不同形式有不同的作用和含义。我们来逐一分析它们的区别。
 *
 * 1. plugins { id("application") }
 * kotlin
 * Copy code
 * plugins {
 *     id("application")
 * }
 * 作用: 直接应用插件。
 * id("application"): 通过插件的 ID 来应用插件。这种方式通常用于官方插件或在 Gradle 插件门户（Gradle Plugin Portal）中发布的插件。
 * application 插件通常用于 Java 应用程序项目，帮助配置打包和运行的相关任务。
 * 应用时机: 插件会立即被应用到当前项目，这意味着插件的所有配置、任务、扩展等都会立即生效。
 * 2. plugins { alias("application") }
 * kotlin
 * Copy code
 * plugins {
 *     alias("application")
 * }
 * 作用: 应用通过版本目录（version catalog）配置的插件。
 * alias("application"): 这里的 alias 指的是通过 Gradle 版本目录（version catalog）定义的一个别名。
 * 版本目录通常存储在 gradle/libs.versions.toml 文件中，其中定义了插件或依赖的版本和别名。
 * 应用时机: 和 id("application") 一样，插件会立即被应用到当前项目。
 * 示例: 在 libs.versions.toml 中定义了 application 别名：
 *
 * toml
 * Copy code
 * [plugins]
 * application = { id = "com.example.application", version = "1.0.0" }
 * 在构建脚本中使用 alias("application")：
 *
 * kotlin
 * Copy code
 * plugins {
 *     alias(libs.plugins.application)
 * }
 * 3. plugins { alias("application") apply false }
 * kotlin
 * Copy code
 * plugins {
 *     alias("application") apply false
 * }
 * 作用: 声明插件，但不立即应用。
 * apply false: 这一关键字表示插件被声明但不会自动应用到当前项目。也就是说，虽然插件被引入到构建脚本中，但其配置、任务和扩展等不会立即生效。
 * 应用时机: 这种方式通常用于多模块项目或其他地方需要手动应用插件的场景。在需要时，插件可以通过 apply 方法在脚本的其他地方手动应用。
 * 示例:
 *
 * kotlin
 * Copy code
 * plugins {
 *     alias("application") apply false
 * }
 *
 * // 在某个模块或特定条件下应用插件
 * project(":submodule") {
 *     apply(plugin = "application")
 * }
 * 总结
 * id("application"): 直接通过插件 ID 应用插件，立即生效。
 * alias("application"): 通过版本目录中的别名应用插件，立即生效。
 * alias("application") apply false: 通过别名声明插件，但不立即应用，需要时手动应用。
 * 使用场景:
 *
 * id("application"): 适合简单项目，直接应用插件。
 * alias("application"): 适合使用版本目录管理依赖的项目，通过别名应用插件。
 * alias("application") apply false: 适合多模块项目或需要条件应用插件的场景，通过别名声明插件，但延迟应用。
 *
 *
 *
 *
 *
 */

plugins {
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.compose)
    alias(libs.plugins.nowinandroid.android.application.flavors)
    alias(libs.plugins.nowinandroid.android.application.jacoco)
    alias(libs.plugins.nowinandroid.android.application.firebase)
    alias(libs.plugins.nowinandroid.hilt)
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
}

android {
    defaultConfig {
        applicationId = "com.google.samples.apps.nowinandroid"
        versionCode = 8
        versionName = "0.1.2" // X.Y.Z; X = Major, Y = minor, Z = Patch level

        // Custom test runner to set up Hilt dependency graph
        testInstrumentationRunner = "com.google.samples.apps.nowinandroid.core.testing.NiaTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = NiaBuildType.DEBUG.applicationIdSuffix
        }
        release {
            isMinifyEnabled = true
            applicationIdSuffix = NiaBuildType.RELEASE.applicationIdSuffix
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            // To publish on the Play store a private signing key is required, but to allow anyone
            // who clones the code to sign and run the release variant, use the debug signing key.
            // TODO: Abstract the signing configuration to a separate file to avoid hardcoding this.
            signingConfig = signingConfigs.named("debug").get()
            // Ensure Baseline Profile is fresh for release builds.
            baselineProfile.automaticGenerationDuringBuild = true
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = "com.google.samples.apps.nowinandroid"
}

dependencies {
    implementation(projects.feature.interests)
    implementation(projects.feature.foryou)
    implementation(projects.feature.bookmarks)
    implementation(projects.feature.topic)
    implementation(projects.feature.search)
    implementation(projects.feature.settings)

    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.analytics)
    implementation(projects.sync.work)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.runtime.tracing)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.window.core)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.coil.kt)

    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.testManifest)
    debugImplementation(projects.uiTestHiltManifest)

    kspTest(libs.hilt.compiler)

    testImplementation(projects.core.dataTest)
    testImplementation(libs.hilt.android.testing)
    testImplementation(projects.sync.syncTest)

    testDemoImplementation(libs.robolectric)
    testDemoImplementation(libs.roborazzi)
    testDemoImplementation(projects.core.screenshotTesting)

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(projects.core.datastoreTest)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)

    baselineProfile(projects.benchmarks)
}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false
}

dependencyGuard {
    configuration("prodReleaseRuntimeClasspath")
}
