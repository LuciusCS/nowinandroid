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

package com.google.samples.apps.nowinandroid.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsHelper
import com.google.samples.apps.nowinandroid.core.data.Synchronizer
import com.google.samples.apps.nowinandroid.core.data.repository.NewsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.SearchContentsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.TopicsRepository
import com.google.samples.apps.nowinandroid.core.datastore.ChangeListVersions
import com.google.samples.apps.nowinandroid.core.datastore.NiaPreferencesDataSource
import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.IO
import com.google.samples.apps.nowinandroid.sync.initializers.SyncConstraints
import com.google.samples.apps.nowinandroid.sync.initializers.syncForegroundInfo
import com.google.samples.apps.nowinandroid.sync.status.SyncSubscriber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * Syncs the data layer by delegating to the appropriate repository instances with
 * sync functionality.
 *
 * 该类继承自 CoroutineWorker，用于在后台执行同步任务。它利用了 Hilt 进行依赖注入，
 * 同时结合了 Kotlin 协程来实现异步操作。下面是对代码的详细解释：
 *
 * @HiltWorker：这是一个自定义的注解，通常用于标识一个 Worker 类，以便与 Hilt 一起使用。它告诉 Hilt 这是一个需要依赖注入的 Worker。
 * internal：将类的可见性限制在模块内，使得该类只能在当前模块中访问。
 * SyncWorker：这是一个类，继承自 CoroutineWorker，用于处理后台任务。
 * @AssistedInject constructor：Hilt 的 @AssistedInject 注解用于处理带有辅助构造参数（即 @Assisted 注解的参数）的依赖注入。
 * 这种情况在 Worker 中常见，因为 Worker 的某些参数（如 Context 和 WorkerParameters）需要在构造时传递。
 */
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    /**
     * @Assisted：这些注解标识的参数是由 WorkManager 提供的，
     * 而不是由 Hilt 提供的。appContext 是应用的 Context，workerParams 包含 Worker 的运行参数。
     */
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    /**
     *  Hilt 注入的依赖项，分别对应不同的功能模块和资源：
     * niaPreferences：处理应用的偏好设置。
     */
    private val niaPreferences: NiaPreferencesDataSource,
    /**
     *  Hilt 注入的依赖项，分别对应不同的功能模块和资源：
     * niaPreferences：处理应用的偏好设置。
     */
    private val topicRepository: TopicsRepository,
    /**
     *  Hilt 注入的依赖项，分别对应不同的功能模块和资源：
     * topicRepository 和 newsRepository：处理与主题和新闻相关的数据存储和同步。
     */
    private val newsRepository: NewsRepository,
    /**
     *  Hilt 注入的依赖项，分别对应不同的功能模块和资源：
     *searchContentsRepository：处理搜索内容的存储和同步。
     */
    private val searchContentsRepository: SearchContentsRepository,
    /**
     *  Hilt 注入的依赖项，分别对应不同的功能模块和资源：
     * @Dispatcher(IO)：指定一个用于 IO 操作的协程调度器，用于确保异步任务在适当的线程池上执行。
     */
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    /**
     *  Hilt 注入的依赖项，分别对应不同的功能模块和资源：
     * analyticsHelper：用于记录分析数据。
     */
    private val analyticsHelper: AnalyticsHelper,
    /**
     *  Hilt 注入的依赖项，分别对应不同的功能模块和资源：
     * syncSubscriber：用于处理同步订阅的逻辑。
     */
    private val syncSubscriber: SyncSubscriber,
    /**
     * CoroutineWorker(appContext, workerParams)：
     * 继承 CoroutineWorker，这是 WorkManager 提供的一个类，用于执行异步后台任务。它支持使用 Kotlin 协程简化异步操作。
     *
     * Synchronizer：实现了一个自定义的 Synchronizer 接口，
     * 该接口可能定义了一些同步相关的功能（例如 getChangeListVersions 和 updateChangeListVersions 方法）。
     *
     */
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    /**
     * getForegroundInfo()：返回 ForegroundInfo，
     * 用于定义前台服务的信息，确保在应用需要长时间运行的任务时，系统不会在后台终止它。通常用于展示通知以保持任务运行。
     */
    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.syncForegroundInfo()

    /**
     * doWork()：Worker 类的核心方法，定义了实际的工作内容。这里使用了 withContext(ioDispatcher)
     * 将任务切换到 IO 调度器，以确保任务在合适的线程池中运行。
     * Result：表示任务的结果，可以是 Result.success()、Result.failure() 或 Result.retry()。
     */
    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        /**
         * traceAsync：一个用于异步操作的跟踪工具，通常用于性能分析和调试。在此处，它用来标识同步任务的开始。
         */
        traceAsync("Sync", 0) {
            /**
             * analyticsHelper.logSyncStarted()：记录同步任务开始的事件。
             */
            analyticsHelper.logSyncStarted()

            /**
             * syncSubscriber.subscribe()：开始同步订阅，这可能涉及初始化同步过程中的某些资源或逻辑。
             *
             */
            syncSubscriber.subscribe()

            // First sync the repositories in parallel
            //awaitAll(...)：等待所有异步任务完成，并返回结果。
            val syncedSuccessfully = awaitAll(
                /**
                 * async { ... }：使用 Kotlin 协程的 async 启动异步任务，
                 * 这里分别对 topicRepository 和 newsRepository 进行并行同步。
                 */
                async { topicRepository.sync() },
                async { newsRepository.sync() },
                /**
                 * all { it }：检查所有任务是否都成功完成。
                 */
            ).all { it }

            /**
             * analyticsHelper.logSyncFinished(syncedSuccessfully)：记录同步任务的完成状态。
             */
            analyticsHelper.logSyncFinished(syncedSuccessfully)

            /**
             * if (syncedSuccessfully)：如果同步成功，调用 searchContentsRepository.populateFtsData()
             * 填充全文本搜索数据，然后返回 Result.success() 表示任务成功完成。
             */
            if (syncedSuccessfully) {
                searchContentsRepository.populateFtsData()
                Result.success()
            } else {
                /**
                 * else：如果同步失败，返回 Result.retry()，告诉 WorkManager 稍后重试该任务
                 */
                Result.retry()
            }
        }
    }

    /**
     * getChangeListVersions()：从偏好设置中获取同步的版本信息。
     */
    override suspend fun getChangeListVersions(): ChangeListVersions =
        niaPreferences.getChangeListVersions()

    /**
     * updateChangeListVersions(update: ChangeListVersions.()
     * -> ChangeListVersions)：更新版本信息，传入一个 lambda 表达式，允许对版本进行修改。
     */
    override suspend fun updateChangeListVersions(
        update: ChangeListVersions.() -> ChangeListVersions,
    ) = niaPreferences.updateChangeListVersion(update)

    /**
     * companion object：Kotlin 中的伴生对象，用于在类级别上提供静态方法和属性。
     */
    companion object {
        /**
         * Expedited one time work to sync data on app startup
         *
         * startUpSyncWork()：创建一个一次性的同步任务，用于应用启动时的数据同步。
         * OneTimeWorkRequestBuilder<DelegatingWorker>()：创建一个一次性 WorkRequest，由 DelegatingWorker 执行。DelegatingWorker 可能是一个代理 Worker，实际执行 SyncWorker 的任务。
         * setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)：设置该任务为加急任务，并且即使任务配额超出限制，也允许任务以非加急方式运行。
         * setConstraints(SyncConstraints)：设置任务的执行约束条件，SyncConstraints 可能指定了网络、充电状态等条件。
         * setInputData(SyncWorker::class.delegatedData())：传递输入数据，这些数据用于 Worker 在执行时使用。
         * build()：构建并返回 WorkRequest。
         */
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .build()
    }
}
