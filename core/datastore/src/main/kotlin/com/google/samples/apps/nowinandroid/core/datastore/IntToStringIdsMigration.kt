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

import androidx.datastore.core.DataMigration

/**
 * Migrates saved ids from [Int] to [String] types
 */
internal object IntToStringIdsMigration : DataMigration<UserPreferences> {

    override suspend fun cleanUp() = Unit

    /***
     * migrate 方法本身确实只负责数据的转换（即通过 copy 创建一个新的 UserPreferences 实例），并不直接执行保存操作。
     * 保存是由 DataStore 框架 在调用 migrate 后自动完成的。这是 DataStore 的核心特性之一，它会在整个迁移过程中管理数据的持久化。
     *
     * 让我们详细分析这一过程：
     */
    override suspend fun migrate(currentData: UserPreferences): UserPreferences =
        currentData.copy {
            // Migrate topic ids
            deprecatedFollowedTopicIds.clear()
            deprecatedFollowedTopicIds.addAll(
                currentData.deprecatedIntFollowedTopicIdsList.map(Int::toString),
            )
            deprecatedIntFollowedTopicIds.clear()

            // Migrate author ids
            deprecatedFollowedAuthorIds.clear()
            deprecatedFollowedAuthorIds.addAll(
                currentData.deprecatedIntFollowedAuthorIdsList.map(Int::toString),
            )
            deprecatedIntFollowedAuthorIds.clear()

            // Mark migration as complete
            hasDoneIntToStringIdMigration = true
        }

    override suspend fun shouldMigrate(currentData: UserPreferences): Boolean =
        !currentData.hasDoneIntToStringIdMigration
}
