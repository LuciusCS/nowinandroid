/*
 * Copyright 2021 The Android Open Source Project
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

pluginManagement {

    /**
     * 在 root project 中 include 这个 build-logic 项目，
     * 因为复合构建会把项目里的构建配置包含进来，所以我们不能直接使用 include(“:build-logic”)，
     * 而是要使用 includeBuild(“build-logic”)
     */

    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "nowinandroid"

/**
 *
 * 不适合 settings.gradle.kts 的上下文:
 * project.feature.foryou 或 projects.feature.foryou 是一种类型安全的路径访问方式，通常用于在构建脚本中引用其他模块。
 * 例如，在某个模块的 build.gradle.kts 文件中定义依赖时，可以使用 implementation(projects.feature.foryou) 来引用其他模块。
 * 然而，在 settings.gradle.kts 中，Gradle 尚未构建项目的依赖图。此时，项目还没有被 include 到构建中，因此不能使用 project.feature.foryou 这样的语法。
 *
 * 执行顺序问题:
 * settings.gradle.kts 中的 include 命令是在构建初始化阶段执行的，目的是告诉 Gradle 哪些模块需要加载。
 * 如果使用 project.feature.foryou 语法，那么就会在初始化阶段试图访问尚未包含在构建中的项目，这会导致构建失败。
 * 语义不同:
 * include 是声明哪些模块存在于项目中，并确定模块的边界。
 * project.feature.foryou 是在构建脚本中，已经包含在构建中的模块之间建立依赖关系或引用。
 *
 *
 * include(":feature:foryou"): 是在 settings.gradle.kts 文件中用来定义项目结构，告诉 Gradle 哪些模块是构建的一部分。这个方法是 Gradle 识别和构建多模块项目的基础。
 * project.feature.foryou: 是在项目的构建脚本（如 build.gradle.kts）中使用的一种类型安全的模块引用方式，用于建立模块之间的依赖关系。
 *
 *
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":app-nia-catalog")
include(":benchmarks")
include(":core:analytics")
include(":core:common")
include(":core:data")
include(":core:data-test")
include(":core:database")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:datastore-test")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:notifications")
include(":core:screenshot-testing")
include(":core:testing")
include(":core:ui")

include(":feature:foryou")
include(":feature:interests")
include(":feature:bookmarks")
include(":feature:topic")
include(":feature:search")
include(":feature:settings")
include(":lint")
include(":sync:work")
include(":sync:sync-test")
include(":ui-test-hilt-manifest")


