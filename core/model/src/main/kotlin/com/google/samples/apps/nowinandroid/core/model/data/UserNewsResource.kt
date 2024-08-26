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

package com.google.samples.apps.nowinandroid.core.model.data

import kotlinx.datetime.Instant

/**
 * A [NewsResource] with additional user information such as whether the user is following the
 * news resource's topics and whether they have saved (bookmarked) this news resource.
 * Primary Constructor: 这是数据类的主要构造函数，它定义了所有属性并通过参数传递它们。这种构造函数用于在类内部或同一模块内创建 UserNewsResource 的实例。
 * internal 修饰符:
 * internal 表示这个构造函数只能在同一模块内使用。也就是说，其他模块无法直接使用这个构造函数来创建 UserNewsResource 实例。
 * 这种设计通常用于控制类的实例化方式，避免外部模块直接访问和使用某些构造方式，从而保持一定的封装性。
 */
data class UserNewsResource internal constructor(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val headerImageUrl: String?,
    val publishDate: Instant,
    val type: String,
    val followableTopics: List<FollowableTopic>,
    val isSaved: Boolean,
    val hasBeenViewed: Boolean,
) {
    /**
     * Secondary Constructor: 这个构造函数提供了一种方便的方式来通过现有的 NewsResource 和 UserData 对象来创建 UserNewsResource 实例。它调用了主构造函数并传递了相应的参数。
     * 作用: 这种设计可以简化对象的创建过程，尤其是当创建 UserNewsResource 需要将多个对象的信息整合到一起时。通过这个构造函数，调用者不需要手动构建所有的属性值，只需要传入 newsResource 和 userData，系统会自动处理这些数据。
     */
    constructor(newsResource: NewsResource, userData: UserData) : this(
        id = newsResource.id,
        title = newsResource.title,
        content = newsResource.content,
        url = newsResource.url,
        headerImageUrl = newsResource.headerImageUrl,
        publishDate = newsResource.publishDate,
        type = newsResource.type,
        followableTopics = newsResource.topics.map { topic ->
            FollowableTopic(
                topic = topic,
                isFollowed = topic.id in userData.followedTopics,
            )
        },
        isSaved = newsResource.id in userData.bookmarkedNewsResources,
        hasBeenViewed = newsResource.id in userData.viewedNewsResources,
    )
}

fun List<NewsResource>.mapToUserNewsResources(userData: UserData): List<UserNewsResource> =
    map { UserNewsResource(it, userData) }
