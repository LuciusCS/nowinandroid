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

package com.google.samples.apps.nowinandroid.core.data

import android.util.Log
import com.google.samples.apps.nowinandroid.core.datastore.ChangeListVersions
import com.google.samples.apps.nowinandroid.core.network.model.NetworkChangeList
import kotlin.coroutines.cancellation.CancellationException

/**
 * Interface marker for a class that manages synchronization between local data and a remote
 * source for a [Syncable].
 *
 * interface Synchronizer: 这是一个用于管理本地数据和远程数据源之间同步的接口。，Synchronizer 负责确保同步操作的顺序性和正确性。
 *
 * 这段代码实现了一个数据同步框架，用于将本地数据库与远程数据源进行同步。
 * Synchronizer 负责管理同步操作并保证同步的顺序性。Syncable 接口表示可同步的类，
 * suspendRunCatching 函数用于安全执行挂起函数并返回结果。
 * changeListSync 函数通过一系列的 lambda 参数实现了同步过程中的读取、删除、更新和版本管理逻辑。
 */
interface Synchronizer {

    /**
     * 这是一个挂起函数，用于获取当前的变更列表版本。ChangeListVersions 应该是一个用于存储版本信息的类。
     */
    suspend fun getChangeListVersions(): ChangeListVersions

    /**
     *  这是另一个挂起函数，用于更新变更列表版本。它接收一个函数作为参数，该函数定义了如何基于当前版本信息来生成新的版本信息。
     */
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)

    /**
     * Syntactic sugar to call [Syncable.syncWith] while omitting the synchronizer argument
     * : 这是一个扩展函数，提供了 Syncable.syncWith 方法的简写形式。调用 Syncable.sync() 时，
     * 实际上是调用 Syncable.syncWith，并将当前 Synchronizer 实例作为参数传入。
     */
    suspend fun Syncable.sync() = this@sync.syncWith(this@Synchronizer)
}

/**
 * Interface marker for a class that is synchronized with a remote source. Syncing must not be
 * performed concurrently and it is the [Synchronizer]'s responsibility to ensure this.
 * interface Syncable: 这是一个标记接口，表示某个类可以与远程数据源同步。具体的同步逻辑由 syncWith 方法实现。
 */
interface Syncable {
    /**
     * Synchronizes the local database backing the repository with the network.
     * Returns if the sync was successful or not.
     * : 这个挂起函数定义了如何使用 Synchronizer 实例与远程数据源进行同步。返回值是一个 Boolean，表示同步是否成功。
     */
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}

/**
 * Attempts [block], returning a successful [Result] if it succeeds, otherwise a [Result.Failure]
 * taking care not to break structured concurrency
 *
 * private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T>:
 * 这是一个私有的泛型挂起函数，接收一个挂起的 block 代码块，并返回一个 Result<T> 对象。
 *
 * 代码块在 try 中运行，如果成功执行，则返回 Result.success，表示成功结果。
 *
 *
 * catch (cancellationException: CancellationException) { throw cancellationException }: 如果捕获到 CancellationException，则重新抛出。CancellationException 是协程取消的标识，不能被吞掉，否则会破坏结构化并发。
 *
 * catch (exception: Exception) { ... Result.failure(exception) }: 捕获其他异常，记录日志后返回 Result.failure，表示失败结果。
 */
private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (exception: Exception) {
    Log.i(
        "suspendRunCatching",
        "Failed to evaluate a suspendRunCatchingBlock. Returning failure Result",
        exception,
    )
    Result.failure(exception)
}

/**
 * Utility function for syncing a repository with the network.
 * [versionReader] Reads the current version of the model that needs to be synced
 * [changeListFetcher] Fetches the change list for the model
 * [versionUpdater] Updates the [ChangeListVersions] after a successful sync
 * [modelDeleter] Deletes models by consuming the ids of the models that have been deleted.
 * [modelUpdater] Updates models by consuming the ids of the models that have changed.
 *
 * Note that the blocks defined above are never run concurrently, and the [Synchronizer]
 * implementation must guarantee this.
 *
 * suspend fun Synchronizer.changeListSync(...): 这是 Synchronizer 的一个扩展挂起函数，
 * 用于同步本地数据和远程数据源。它接收多个 lambda 参数，用于定制同步逻辑。
 *
 *
 */
suspend fun Synchronizer.changeListSync(
    /**
     * versionReader: (ChangeListVersions) -> Int: 这个参数是一个函数，读取当前变更列表版本并返回一个整数（通常是版本号）。
     *
     */
    versionReader: (ChangeListVersions) -> Int,
    /**
     * changeListFetcher: suspend (Int) -> List<NetworkChangeList>: 这是一个挂起函数，接收一个版本号并返回从该版本号开始的变更列表。
     */
    changeListFetcher: suspend (Int) -> List<NetworkChangeList>,
    /**
     * changeListFetcher: suspend (Int) -> List<NetworkChangeList>: 这是一个挂起函数，接收一个版本号并返回从该版本号开始的变更列表。
     */
    versionUpdater: ChangeListVersions.(Int) -> ChangeListVersions,
    /**
     * modelDeleter: suspend (List<String>) -> Unit: 这是一个挂起函数，负责删除本地数据库中已经被远程删除的模型。
     */
    modelDeleter: suspend (List<String>) -> Unit,
    /**
     * modelUpdater: suspend (List<String>) -> Unit: 这是一个挂起函数，负责更新本地数据库中需要更新的模型。
     */
    modelUpdater: suspend (List<String>) -> Unit,

    /**
     * suspendRunCatching { ... }: changeListSync 函数的核心逻辑被包裹在 suspendRunCatching 中，以确保即使发生异常，也能得到一个 Result 对象。
     */
) = suspendRunCatching {
    // Fetch the change list since last sync (akin to a git fetch)

    /**
     * val currentVersion = versionReader(getChangeListVersions()): 获取当前的变更列表版本。
     */
    val currentVersion = versionReader(getChangeListVersions())

    /**
     * val changeList = changeListFetcher(currentVersion): 获取自上次同步以来的变更列表，类似于 Git 中的 fetch 操作。
     *
     *
     */
    val changeList = changeListFetcher(currentVersion)
    /**
     * if (changeList.isEmpty()) return@suspendRunCatching true: 如果没有变更，直接返回 true，表示同步成功。
     */
    if (changeList.isEmpty()) return@suspendRunCatching true

    /**
     * val (deleted, updated) = changeList.partition(NetworkChangeList::isDelete): 根据变更类型将变更列表分为删除和更新两部分。
     */
    val (deleted, updated) = changeList.partition(NetworkChangeList::isDelete)

    // Delete models that have been deleted server-side
    /**
     * modelDeleter(deleted.map(NetworkChangeList::id)): 删除本地数据库中已经被远程删除的模型。
     */
    modelDeleter(deleted.map(NetworkChangeList::id))

    // Using the change list, pull down and save the changes (akin to a git pull)
    /**
     * modelUpdater(updated.map(NetworkChangeList::id)): 使用更新列表拉取并保存变更，类似于 Git 中的 pull 操作。
     */
    modelUpdater(updated.map(NetworkChangeList::id))

    // Update the last synced version (akin to updating local git HEAD)
    /**
     * val latestVersion = changeList.last().changeListVersion: 获取变更列表中的最新版本号。
     */
    val latestVersion = changeList.last().changeListVersion
    /**
     * updateChangeListVersions { versionUpdater(latestVersion) }: 更新变更列表版本到最新版本号。
     */
    updateChangeListVersions {
        versionUpdater(latestVersion)
    }
}.isSuccess
