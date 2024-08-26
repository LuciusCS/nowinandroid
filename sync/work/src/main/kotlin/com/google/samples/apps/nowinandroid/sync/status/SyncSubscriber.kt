/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.sync.status

/**
 * Subscribes to backend requested synchronization
 *
 * 实现类是 StubSyncSubscriber
 *
 * StubSyncSubscriber实现了SyncSubscriber接口，并且通过@Inject注解标记，Hilt会将它作为SyncSubscriber的实现类注入到SyncWorker中。
 *
 * Hilt通过注解和代码生成机制，自动处理SyncSubscriber接口与其实现类StubSyncSubscriber之间的依赖关系，并将它注入到需要的地方。
 *
 */
interface SyncSubscriber {
    suspend fun subscribe()
}
