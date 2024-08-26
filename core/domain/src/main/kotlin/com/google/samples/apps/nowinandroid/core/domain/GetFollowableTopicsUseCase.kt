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

package com.google.samples.apps.nowinandroid.core.domain

import com.google.samples.apps.nowinandroid.core.data.repository.TopicsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.domain.TopicSortField.NAME
import com.google.samples.apps.nowinandroid.core.domain.TopicSortField.NONE
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * A use case which obtains a list of topics with their followed state.
 *  定义了一个类 GetFollowableTopicsUseCase。@Inject 注解表明这个类的构造函数会由依赖注入框架（如 Dagger 或 Hilt）自动提供所需的依赖项。
 *  这样可以在创建 GetFollowableTopicsUseCase 对象时自动注入 TopicsRepository 和 UserDataRepository 实例。
 */
class GetFollowableTopicsUseCase @Inject constructor(
    private val topicsRepository: TopicsRepository,
    private val userDataRepository: UserDataRepository,
) {
    /**
     * Returns a list of topics with their associated followed state.
     *
     * @param sortBy - the field used to sort the topics. Default NONE = no sorting.
     *
     * invoke 运算符: 在 Kotlin 中，使用 operator fun invoke 定义的函数可以让类的实例像函数一样被调用。
     * 例如，如果有一个 GetFollowableTopicsUseCase 的实例 useCase，
     * 你可以使用 useCase() 来调用这个 invoke 函数。这种设计让代码更简洁直观，尤其是在频繁调用时。
     *
     * operator fun invoke: 定义了一个 invoke 运算符函数。在 Kotlin 中，invoke 函数允许类的实例像函数一样被调用。
     * 这样，GetFollowableTopicsUseCase 类的实例可以通过 instance() 语法直接调用，而不需要显式地调用函数名。
     *
     *
     * 这个 invoke 运算符函数允许你通过直接调用对象名（如 getFollowableTopics）来触发 invoke 函数的逻辑。
     *
     *
     *
     * sortBy: TopicSortField = NONE: 函数的参数 sortBy 用于指定对主题进行排序的字段，默认值是 NONE（即不进行排序）。
     *
     * Flow<List<FollowableTopic>>: 函数返回一个 Flow 对象，该对象发射包含 FollowableTopic 列表的数据流。
     *
     *
     * 为什么不用 useCase:
     * 如果构造函数的参数名是 getFollowableTopics，那么这个名字就可以直接用于调用 invoke 函数。如果参数名是 useCase，
     * 你可以用 useCase(sortBy = TopicSortField.NAME) 来调用同样的函数。但通常使用一个更加语义化的名称（如 getFollowableTopics），
     * 可以让代码更清晰易懂。
     *
     * 在这里，getFollowableTopics(sortBy = TopicSortField.NAME) 实际上等价于
     * getFollowableTopics.invoke(sortBy = TopicSortField.NAME)，但因为 invoke 运算符允许省略函数名，使得代码更简洁。
     *
     *
     * 在 GetFollowableTopicsUseCase 中的 invoke 函数虽然没有使用显式的 return 关键字，但它确实有返回值。
     * Kotlin 函数如果只有一个表达式，可以使用简写的方式，这种情况下，函数会隐式地返回该表达式的结果。
     *
     */
    operator fun invoke(sortBy: TopicSortField = NONE): Flow<List<FollowableTopic>> = combine(

        /**
         *  使用了 combine 函数，它用于组合来自多个流的数据（userDataRepository.userData
         *  和 topicsRepository.getTopics()），并将它们合并为一个新的流。
         */

        /**
         *  获取用户数据的流，userDataRepository.userData 是一个 Flow<UserData>，它持续发射用户数据的变化。
         */
        userDataRepository.userData,
        /**
         * : 获取主题数据的流，topicsRepository.getTopics() 是一个 Flow<List<Topic>>，它持续发射主题列表的变化。
         */
        topicsRepository.getTopics(),
    ) {
        /**
         * combine 函数中的 lambda 表达式，接收两个参数：userData（来自用户数据流）和 topics（来自主题流）。
         * 这个 lambda 用于处理和合并这两个流的数据。
         */
      userData, topics ->

        /**
         * 定义一个局部变量 followedTopics，它保存处理后的 FollowableTopic 列表。
         */
        val followedTopics = topics
            /**
             * 使用 map 函数遍历 topics 列表中的每一个 topic，并将其转换为 FollowableTopic 对象。
             */
            .map { topic ->

                /**
                 * 创建一个新的 FollowableTopic 对象。FollowableTopic 是一个数据类，用于封装一个 Topic 及其对应的 isFollowed 状态。
                 */
                FollowableTopic(
                    topic = topic,
                    isFollowed = topic.id in userData.followedTopics,
                )
            }
        when (sortBy) {
            NAME -> followedTopics.sortedBy { it.topic.name }
            else -> followedTopics
        }
    }
}

enum class TopicSortField {
    NONE,
    NAME,
}
