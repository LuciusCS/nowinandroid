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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import com.google.samples.apps.nowinandroid.sync.R

/**
 * 定义了一个用于同步任务的前台服务通知，确保在设备运行较低 API 版本时，前台同步任务不会被系统杀死。
 * 通知通道的创建确保在 Android O 及更高版本上正确管理通知，而同步约束条件则确保同步任务只在设备连接到网络时执行。
 *
 */

/**
 * const val SYNC_TOPIC = "sync"：定义了一个同步相关的常量主题名称，可以用于日志记录、事件跟踪或其他需要标识同步操作的地方。
 */
const val SYNC_TOPIC = "sync"

/**
 * private const val SYNC_NOTIFICATION_ID = 0：定义了通知的唯一标识符。0 作为通知 ID，可以用来更新或取消通知。
 */
private const val SYNC_NOTIFICATION_ID = 0

/**
 * private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"：
 * 定义了通知通道的 ID，用于在 API 26+ 的设备上创建通知通道。
 */
private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

// All sync work needs an internet connectionS
/**
 * val SyncConstraints：这是一个 val 属性，它返回一个构建好的 Constraints 对象。
 * Constraints.Builder()：使用 Constraints.Builder() 创建一个新的 Constraints 对象，
 * Constraints 用于定义 WorkManager 执行任务时需要满足的条件。
 * .setRequiredNetworkType(NetworkType.CONNECTED)：设置执行同步任务时必须有网络连接。这意味着如果设备当前没有网络连接，WorkManager 将推迟任务的执行，直到网络连接恢复。
 * .build()：构建并返回 Constraints 对象。
 */
val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

/**
 * Foreground information for sync on lower API levels when sync workers are being
 * run with a foreground service
 * un Context.syncForegroundInfo(): 这是一个扩展函数，扩展了 Context 类。它用于返回 ForegroundInfo 对象，定义前台服务的信息。
 * ForegroundInfo(SYNC_NOTIFICATION_ID, syncWorkNotification())：创建一个 ForegroundInfo 对象。SYNC_NOTIFICATION_ID 是通知 ID，syncWorkNotification() 是生成的通知对象。
 * 这个 ForegroundInfo 用于在较低 API 级别上运行同步任务时，展示前台通知以确保任务不会被系统杀死。
 */
fun Context.syncForegroundInfo() = ForegroundInfo(
    SYNC_NOTIFICATION_ID,
    syncWorkNotification(),
)

/**
 * Notification displayed on lower API levels when sync workers are being
 * run with a foreground service
 * private fun Context.syncWorkNotification(): Notification: 这是一个扩展函数，
 * 扩展了 Context 类。用于创建并返回一个通知对象，该通知用于在前台服务中显示。
 */
private fun Context.syncWorkNotification(): Notification {
    /**
     * 检查当前设备的 Android 版本是否为 Android O（API 26）或更高版本。在这些版本中，通知必须绑定到通知通道。
     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        /**
         * val channel = NotificationChannel(...)：创建一个新的通知通道，使用 SYNC_NOTIFICATION_CHANNEL_ID 作为通道 ID，
         */
        val channel = NotificationChannel(
            SYNC_NOTIFICATION_CHANNEL_ID,
            //getString(R.string.sync_work_notification_channel_name) 作为通道名称，
            getString(R.string.sync_work_notification_channel_name),
            //NotificationManager.IMPORTANCE_DEFAULT 作为通知的默认重要级别。
            NotificationManager.IMPORTANCE_DEFAULT,
            /***
             * apply { ... }：apply 是一个作用域函数，用于配置 NotificationChannel 对象的属性。在这里，description 被设置为通知通道的描述。
             */
        ).apply {
            description = getString(R.string.sync_work_notification_channel_description)
        }
        // Register the channel with the system
        /**
         * val notificationManager: NotificationManager? = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager：
         * 获取系统的 NotificationManager 服务，用于管理通知。
         */
        val notificationManager: NotificationManager? =
            getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        /**
         * notificationManager?.createNotificationChannel(channel)：将新创建的通知通道注册到系统中。
         */
        notificationManager?.createNotificationChannel(channel)
    }

    /**
     * return NotificationCompat.Builder(...)：创建并返回一个 NotificationCompat.Builder 对象，
     * 用于构建通知。这个对象用于在前台服务中展示通知。
     * this：Context 对象的引用。
     * SYNC_NOTIFICATION_CHANNEL_ID：通知通道的 ID，确保通知与合适的通道关联。
     * .setSmallIcon(...)：设置通知的小图标，这里引用了 drawable 资源中的图标。
     * .setContentTitle(getString(R.string.sync_work_notification_title))：设置通知的标题，通过 getString 获取资源中的字符串。
     * .setPriority(NotificationCompat.PRIORITY_DEFAULT)：设置通知的优先级为默认优先级，表示通知的重要性适中。
     * .build()：构建并返回 Notification 对象。
     */
    return NotificationCompat.Builder(
        this,
        SYNC_NOTIFICATION_CHANNEL_ID,
    )
        .setSmallIcon(
            com.google.samples.apps.nowinandroid.core.notifications.R.drawable.core_notifications_ic_nia_notification,
        )
        .setContentTitle(getString(R.string.sync_work_notification_title))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
}
