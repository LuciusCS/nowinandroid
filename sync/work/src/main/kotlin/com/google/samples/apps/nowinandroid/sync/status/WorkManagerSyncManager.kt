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

package com.google.samples.apps.nowinandroid.sync.status

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import com.google.samples.apps.nowinandroid.core.data.util.SyncManager
import com.google.samples.apps.nowinandroid.sync.initializers.SYNC_WORK_NAME
import com.google.samples.apps.nowinandroid.sync.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [SyncManager] backed by [WorkInfo] from [WorkManager]
 *
 *
 * 当Hilt看到ForYouViewModel需要一个SyncManager时，
 * 它会在依赖关系图中查找哪些类实现了SyncManager接口。如果只有一个类实现了该接口，
 * 并且这个类（如WorkManagerSyncManager）的构造函数上有@Inject注解，Hilt会自动实例化WorkManagerSyncManager
 * 并将其注入到syncManager参数中。
 *
 *
 *
 * 多个实现：如果有多个实现SyncManager的类，Hilt会无法确定哪个类应该被注入。这时，你需要使用@Qualifier注解来指定具体的实现。
 * 模块化配置：如果WorkManagerSyncManager不是直接通过@Inject构造函数提供的，而是通过@Provides或@Binds提供，Hilt仍然会按照配置来注入正确的实现。
 *
 *
 * 这段代码通过WorkManager调度和管理同步任务，确保应用启动时只运行一个同步任务。
 * 它使用Flow来持续监控同步任务的运行状态，并暴露一个isSyncing属性来表示同步是否正在进行。
 * requestSync方法通过enqueueUniqueWork确保同一时间只运行一个名为SYNC_WORK_NAME的同步任务。
 *
 *
 *  原理总结
 * 依赖解析：Hilt通过依赖关系图（Dependency Graph）来解析SyncManager接口，并确定WorkManagerSyncManager是唯一的实现类。
 * 依赖注入：在实例化ForYouViewModel时，Hilt会自动创建WorkManagerSyncManager实例，并将其注入到syncManager参数中。
 * 自动管理：Hilt管理WorkManagerSyncManager的生命周期，并确保在ViewModel销毁时，所有依赖关系都能被正确处理。
 *
 *
 */
internal class WorkManagerSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncManager {
    /**
     * override val isSyncing：重写SyncManager接口中的isSyncing属性。
     * 这个属性是一个Flow<Boolean>类型，表示一个异步的数据流，用于跟踪同步任务是否正在运行。
     */
    override val isSyncing: Flow<Boolean> =

        /**
         * WorkManager.getInstance(context)：获取WorkManager的实例，用于管理后台任务。WorkManager是Google提供的一个用于调度后台任务的库。
         * getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)：
         * 获取特定任务名称（SYNC_WORK_NAME）对应的工作信息流（Flow<List<WorkInfo>>）。WorkInfo包含了关于任务执行状态的信息。
         */
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            /**
             * .map(List<WorkInfo>::anyRunning)：使用map操作符将Flow<List<WorkInfo>>映射为Flow<Boolean>。
             * List<WorkInfo>::anyRunning是一个函数引用，用于判断列表中是否有任务正在运行。
             */
            .map(List<WorkInfo>::anyRunning)
            /**
             * .conflate()：使用conflate操作符优化数据流的处理。
             * 如果有多个任务状态更新，它会跳过中间的无关状态，确保只处理最新的状态。这样可以减少不必要的资源消耗。
             */
            .conflate()

    /**
     * override fun requestSync()：实现SyncManager接口中的requestSync方法，用于请求启动同步任务。
     */
    override fun requestSync() {
        /**
         * val workManager = WorkManager.getInstance(context)：获取WorkManager实例，用于调度后台任务。
         */
        val workManager = WorkManager.getInstance(context)
        // Run sync on app startup and ensure only one sync worker runs at any time
        /**
         * workManager.enqueueUniqueWork(：调用enqueueUniqueWork方法，调度一个唯一的后台任务。此方法确保具有相同名称的任务不会重复执行。
         */
        workManager.enqueueUniqueWork(
            /**
             * SYNC_WORK_NAME：指定任务的唯一名称，通常是一个字符串常量。这个名称用于标识后台任务，以便WorkManager管理它。
             */
            SYNC_WORK_NAME,
            /**
             * ExistingWorkPolicy.KEEP：定义当同名任务已经存在时的处理策略。KEEP策略表示，如果有同名任务正在运行，则保留该任务，不启动新的任务。
             */
            ExistingWorkPolicy.KEEP,
            /**
             * SyncWorker.startUpSyncWork(),：调用SyncWorker的静态方法startUpSyncWork()，创建一个新的OneTimeWorkRequest对象，代表将要执行的同步任务。
             */
            SyncWorker.startUpSyncWork(),
        )
    }
}

/**
 * private fun List<WorkInfo>.anyRunning() = any { it.state == State.RUNNING }：定义了一个扩展函数anyRunning()，用于检查List<WorkInfo>中是否有任务的状态为RUNNING。如果有，返回true；否则，返回false。
 */
private fun List<WorkInfo>.anyRunning() = any { it.state == State.RUNNING }
