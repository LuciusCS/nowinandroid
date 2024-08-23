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

package com.google.samples.apps.nowinandroid.di

import android.app.Activity
import android.util.Log
import android.view.Window
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.JankStats.OnFrameListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * 这段代码是一个使用Dagger-Hilt的模块定义，专门用于配置和提供JankStats相关的依赖。JankStats是一种用于监控应用中卡顿现象（jank）的工具。下面是对代码中每一行的详细解释：
 *
 * @Module Dagger的注解，表明这个类是一个模块，里面定义了提供依赖的方法。
 *
 * @InstallIn(ActivityComponent::class)：这是Dagger-Hilt的注解，
 * 指定这个模块的作用范围是ActivityComponent，即这些依赖将在Activity的生命周期内被提供。
 *
 *
 * object JankStatsModule：声明一个Kotlin对象，表示这是一个单例类。Dagger-Hilt会在需要时自动实例化这个模块。
 *
 */
@Module
@InstallIn(ActivityComponent::class)
object JankStatsModule {

    /**
     * @Provides：Dagger的注解，标记该方法将提供一个依赖对象。
     *
     *  providesOnFrameListener() 定义一个函数，用于提供OnFrameListener实例。
     *
     *
     *  OnFrameListener { frameData -> ... }：创建一个OnFrameListener，它是JankStats用来监听每一帧的回调接口。frameData包含了关于当前帧的信息。
     */
    @Provides
    fun providesOnFrameListener(): OnFrameListener = OnFrameListener { frameData ->
        // Make sure to only log janky frames.
        /**
         * 检查当前帧是否为卡顿帧（janky frame）
         */
        if (frameData.isJank) {
            // We're currently logging this but would better report it to a backend.
            /**
             * Log.v("NiA Jank", frameData.toString())：如果是卡顿帧，记录日志。目前只是在日志中输出，可以扩展为将数据报告到后端。
             */
            Log.v("NiA Jank", frameData.toString())
        }
    }

    /**
     * 定义一个函数，用于从当前Activity中获取Window实例。
     *
     * activity.window：返回当前Activity的Window对象。Window是应用UI的顶级容器，JankStats需要它来跟踪UI的渲染。
     */
    @Provides
    fun providesWindow(activity: Activity): Window = activity.window

    /**
     * fun providesJankStats(window: Window, frameListener: OnFrameListener):
     * JankStats：该函数接收Window和OnFrameListener作为参数，并返回一个JankStats实例。
     * JankStats.createAndTrack(window, frameListener)：
     * 使用JankStats的createAndTrack静态方法创建并返回一个JankStats实例，并立即开始跟踪通过window渲染的帧，并将帧数据传递给frameListener。
     */
    @Provides
    fun providesJankStats(
        window: Window,
        frameListener: OnFrameListener,
    ): JankStats = JankStats.createAndTrack(window, frameListener)
}
