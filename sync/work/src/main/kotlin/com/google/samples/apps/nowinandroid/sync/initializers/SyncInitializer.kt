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

package com.google.samples.apps.nowinandroid.sync.initializers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.samples.apps.nowinandroid.sync.workers.SyncWorker

/**
 * 用于初始化和管理应用数据同步任务的 Kotlin 类，利用 WorkManager 来确保在应用启动时自动进行数据同步，并且防止多个同步任务同时运行x
 * object Sync：定义了一个名为 Sync 的 Kotlin 单例对象。单例对象在整个应用中只有一个实例，适合用于管理全局状态或任务。
 */
object Sync {
    // This method is initializes sync, the process that keeps the app's data current.
    // It is called from the app module's Application.onCreate() and should be only done once.
    ///释说明了这个方法的作用，即初始化同步过程，保持应用数据的最新状态。这个方法通常在应用的
    // Application 类的 onCreate() 方法中调用，并且应该只调用一次，以确保任务初始化的唯一性。
    fun initialize(context: Context) {
        /**
         * apply { ... }：apply 是一个 Kotlin 的作用域函数，允许在对象上调用多个方法并返回该对象本身。
         * 在这里，apply 使得我们可以直接在 WorkManager 实例上调用方法。
         */
        WorkManager.getInstance(context).apply {
            // Run sync on app startup and ensure only one sync worker runs at any time
            //将一个唯一的工作任务加入 WorkManager 的队列
            enqueueUniqueWork(
                //任务的唯一名称，确保在队列中这个任务是唯一的。
                SYNC_WORK_NAME,
                ///指定任务的处理策略。如果已经存在一个同名的任务在队列中执行，则保持现有任务继续执行，而不启动新任务。这保证了不会有多个相同的同步任务并发运行。
                ExistingWorkPolicy.KEEP,
                //这是一个 WorkRequest，定义了同步任务的具体工作内容。SyncWorker 是一个自定义的 Worker 类，负责执行同步操作。
                SyncWorker.startUpSyncWork(),
            )
        }
    }
}

// This name should not be changed otherwise the app may have concurrent sync requests running
/**
 * internal const val SYNC_WORK_NAME = "SyncWorkName"：定义了一个内部常量 SYNC_WORK_NAME，表示同步任务的名称。const val 表示这是一个编译时常量，值不可更改。
 * internal：表示这个常量的可见性是模块内的，即它只能在同一个模块中访问。这个常量用于确保 WorkManager 中同步任务的唯一性。
 * 这个名称不能被更改，否则可能会导致应用中同时运行多个同步请求，违背了任务唯一性的设计。
 */
internal const val SYNC_WORK_NAME = "SyncWorkName"
