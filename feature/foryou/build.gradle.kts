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

plugins {
    alias(libs.plugins.nowinandroid.android.feature)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.library.jacoco)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.google.samples.apps.nowinandroid.feature.foryou"
}

dependencies {
    ///依赖其他组件
    implementation(libs.accompanist.permissions)
    implementation(projects.core.data)
    implementation(projects.core.domain)

    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.robolectric)
    testImplementation(projects.core.testing)
    testDemoImplementation(projects.core.screenshotTesting)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
    androidTestImplementation(projects.core.testing)
}

/**
 *
 * 在 Gradle 构建脚本中，dependencies 块用于声明项目的依赖关系。你提到的两种不同的方式用于在多模块项目中添加依赖，它们的主要区别在于依赖的定义方式和管理方式。
 *
 * 1. implementation(projects.core.data) 和 implementation(projects.core.domain)
 * kotlin
 * Copy code
 * dependencies {
 *     implementation(projects.core.data)
 *     implementation(projects.core.domain)
 * }
 * 基于类型安全的依赖管理:
 * 这种方式通常是在 Gradle 的 类型安全的项目访问器的基础上使用的。
 * 你通过 projects 对象访问项目模块，这是一种类型安全的方式，可以通过 IDE 的自动补全功能发现和选择模块。
 * 这种方法依赖于 Gradle Kotlin DSL（.kts 脚本文件），并且 projects 对象通常是通过项目的 settings.gradle.kts 中配置的 versionCatalog 或者 build.gradle.kts 文件自动生成的。
 * 本项目中应该是 libs.version.toml
 *
 * 优势:
 * 类型安全: 由于 projects 对象是类型安全的，所以在重构、重命名或移动模块时，编译器可以帮助你捕获错误。
 * 可读性好: 使用这种方式，依赖关系清晰，尤其是在大型项目中，可以帮助减少人为错误。
 * 2. implementation(project(":core:shared"))
 * kotlin
 * Copy code
 * dependencies {
 *     implementation(project(":core:shared"))
 *     implementation(project(":core:shared"))
 * }
 * 基于路径的依赖管理:
 * 这种方式是通过指定项目的路径来添加依赖。这里 project(":core:shared") 表示依赖于 :core:shared 模块，这个路径是在 settings.gradle 中定义的项目路径。
 * 这是 Gradle 脚本中的传统方式，无论是 Groovy DSL（.gradle）还是 Kotlin DSL（.gradle.kts）都可以使用。
 * 优势:
 * 简洁且兼容性好: 这是更通用的方式，适用于所有类型的 Gradle 构建脚本，尤其是在不使用类型安全访问器的情况下。
 * 易于理解: 项目路径是直观的，对于不熟悉 Kotlin DSL 的开发者而言，这种方式更易于理解和使用。
 * 总结与对比
 * 定义方式:
 * 类型安全方式 (projects.core.data): 更加现代和安全，通过 Kotlin DSL 提供的类型安全访问器来管理模块依赖。它在重构和维护过程中具有更高的安全性和可读性。
 * 基于路径方式 (project(":core:shared")): 更传统和通用，使用项目路径来管理模块依赖，适用于 Groovy DSL 和 Kotlin DSL，易于理解和广泛使用。
 * 可维护性:
 * 类型安全方式: 在大型项目中更容易维护，因为它可以利用 IDE 的自动补全和类型检查，减少错误。
 * 基于路径方式: 虽然简洁，但在项目重构时更容易出现错误，需要手动更新依赖路径。
 * 使用场景:
 * 类型安全方式: 推荐在使用 Kotlin DSL 的项目中使用，尤其是在需要大量模块化管理的复杂项目中。
 * 基于路径方式: 适用于简单项目或不使用 Kotlin DSL 的项目，也适用于需要兼容旧式 Gradle 构建脚本的场景。
 * 这两种方式的选择主要取决于项目的复杂度、团队的习惯以及使用的 Gradle DSL 类型。如果你的项目是现代化的、基于 Kotlin DSL 的，并且需要较强的可维护性，
 * 推荐使用类型安全的方式。如果是传统项目或者你更习惯基于路径的管理，使用基于路径的方式也完全可行。
 */
