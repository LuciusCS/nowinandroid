/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.core.network.demo

import JvmUnitTestDemoAssetManager
import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.IO
import com.google.samples.apps.nowinandroid.core.network.NiaNetworkDataSource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkChangeList
import com.google.samples.apps.nowinandroid.core.network.model.NetworkNewsResource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkTopic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

/**
 * [NiaNetworkDataSource] implementation that provides static news resources to aid development
 */
class DemoNiaNetworkDataSource @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    /**
     * networkJson: Json: 注入 Json 对象，用于解析网络响应。
     */
    private val networkJson: Json,
    /**
     * assets: DemoAssetManager = JvmUnitTestDemoAssetManager:
     * 默认使用 JvmUnitTestDemoAssetManager 作为 DemoAssetManager，
     * 但在生产环境下，Dagger Hilt 会注入 providesDemoAssetManager 提供的实现。
     */
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager,
) : NiaNetworkDataSource {

    /**
     *
     *
     * suspend 其实就是kotlin编译器帮我们做的事情，结合协程，他让我们能用同步的方式写异步的代码，避免了回调地狱。这里有点跑题了，我们来逐个分析下，这里的执行过程。
     * ‘
     *
     * 表示代码中使用了Kotlin序列化库的实验性API。使用这个注解表明你知道正在使用不稳定的功能，这些功能可能会在未来的版本中发生变化。
     *
     * 这是一个override方法，重写了父类或接口中的方法。该方法是getTopics，
     * 它是一个Suspend函数，可以在协程中异步调用。它接收一个可选的List<String>参数ids，返回一个List<NetworkTopic>类型的对象。
     */

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getTopics(ids: List<String>?): List<NetworkTopic> =

        /**
         *  这个代码块将协程的执行上下文切换到ioDispatcher，这通常用于I/O密集型操作，如文件读取、网络请求等。ioDispatcher通常配置为一个专门用于I/O操作的线程池。
         */
        withContext(ioDispatcher) {
            /**
             * assets.open(TOPICS_ASSET): 通过assets对象打开名为TOPICS_ASSET的资产文件，这个文件名为topics.json。
             *
             * .use {}: 在执行完块中的代码后，会自动关闭文件流，以防止资源泄漏。
             *
             * networkJson::decodeFromStream: 这是一个函数引用，
             * 指向一个用于将输入流解码为NetworkTopic对象的函数。这里是将JSON文件流解码为NetworkTopic列表。
             *
             */
            assets.open(TOPICS_ASSET).use(networkJson::decodeFromStream)
        }

    /**
     * 这是另一个重写方法getNewsResources，同样是一个Suspend函数。它接收一个可选的List<String>参数ids，返回一个List<NetworkNewsResource>类型的对象。
     */
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getNewsResources(ids: List<String>?): List<NetworkNewsResource> =
        withContext(ioDispatcher) {
            assets.open(NEWS_ASSET).use(networkJson::decodeFromStream)
        }

    override suspend fun getTopicChangeList(after: Int?): List<NetworkChangeList> =
        /**
         * getTopics(): 调用getTopics方法，获取NetworkTopic对象的列表。
         * mapToChangeList(NetworkTopic::id):
         * 将每个NetworkTopic对象映射为一个NetworkChangeList对象，这里通过NetworkTopic::id来获取对象的唯一标识符。
         */
        getTopics().mapToChangeList(NetworkTopic::id)

    /**
     * 这是另一个重写方法getNewsResourceChangeList，返回一个List<NetworkChangeList>类型的对象。它接收一个可选的Int参数after，表示获取的更改列表中的起始位置。
     */
    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
        getNewsResources().mapToChangeList(NetworkNewsResource::id)

    /**
     * companion object {
     * 解释: 这是一个companion object，相当于类的静态部分，包含静态常量或方法。
     */
    companion object {
        private const val NEWS_ASSET = "news.json"
        private const val TOPICS_ASSET = "topics.json"
    }
}

/**
 *
 * 在 Kotlin 中，companion object 的使用和常量的定义位置取决于几个因素。使用 companion object 来定义常量与直接在类中定义它们有以下几点不同：
 *
 * 1. 静态上下文
 * companion object: 这是一个与类关联的静态对象，Kotlin 会自动为其生成与 Java 中静态成员类似的静态成员。这意味着在类的任何实例化对象之外，你可以通过类名直接访问这些常量。例如：
 *
 * kotlin
 * Copy code
 * ClassName.NEWS_ASSET
 * 直接在类中定义: 如果你在类中直接定义常量，而不使用 companion object，这些常量将不是静态的，它们与每个类的实例关联，无法通过类名直接访问。
 *
 * 2. 内存使用和访问
 * companion object: 常量被定义为静态成员，这意味着它们在内存中只存在一个实例，在整个应用程序生命周期内是共享的。通过 companion object 定义常量，避免了在每个实例化的对象中都创建这些常量，节省了内存。
 *
 * 直接在类中定义: 如果直接在类中定义这些常量，每个类实例都会有这些常量的副本，这可能会增加内存使用，特别是在大量创建类实例时。
 *
 * 3. 使用场景
 * companion object: 如果常量只与类本身有关，且不依赖于类的实例，那么使用 companion object 是更好的选择。它使得这些常量更容易访问和管理。
 *
 * 直接在类中定义: 如果常量与类的实例密切相关，且可能会根据实例的不同而变化（虽然这里定义的是 const，理论上不可变，但仍有可能因为逻辑上的原因不适合定义在 companion object），那么可以考虑在类中直接定义。
 *
 * 4. 约定俗成
 * Kotlin 中，通常会将与类相关但不依赖于实例的常量放在 companion object 中，这是一个普遍接受的代码组织方式，符合最佳实践。
 * 结论
 * 在你的代码中，NEWS_ASSET 和 TOPICS_ASSET 是与类本身相关的常量，它们不依赖于类的实例，因此将它们放在 companion object 中使得这些常量成为类的静态成员，便于直接通过类名访问，节省内存，并符合 Kotlin 的最佳实践。
 *
 */

/**
 *
 *
 * class DemoNiaNetworkDataSource @Inject constructor(
 *     @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
 *     private val networkJson: Json,
 *     private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager,
 * ) : NiaNetworkDataSource {}
 *
 *
 * 在这段代码中，DemoAssetManager 的默认实现被设定为 JvmUnitTestDemoAssetManager，
 * 但在生产环境中，Dagger Hilt 会注入 providesDemoAssetManager 提供的实现。
 * 理解这个机制涉及 Kotlin 默认参数与 Dagger Hilt 的依赖注入机制的结合。
 *
 * 1. Kotlin 默认参数机制
 * private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager 表明 assets 参数具有一个默认值 JvmUnitTestDemoAssetManager。
 * 在 Kotlin 中，构造函数的参数可以指定默认值。当使用默认值时，如果在调用构造函数时没有传递该参数，则会使用默认值。
 * 2. Dagger Hilt 依赖注入的优先级
 * @Inject constructor 标记了 DemoNiaNetworkDataSource 的构造函数，表明这个类的实例化将由 Dagger Hilt 来管理。
 * 当 Dagger Hilt 创建 DemoNiaNetworkDataSource 实例时，它会尝试为构造函数中的每个参数注入依赖。
 * 3. 注入过程的工作原理
 * 生产环境中：
 *
 * 在生产环境中，Dagger Hilt 会扫描所有标注了 @Module 和 @Provides 的方法。
 * 当 Hilt 发现 DemoNiaNetworkDataSource 需要一个 DemoAssetManager 类型的依赖时，它会找到 providesDemoAssetManager 方法并调用它，返回一个 DemoAssetManager 实例（由 context.assets::open 实现）。
 * 即使 assets 参数有默认值 JvmUnitTestDemoAssetManager，Hilt 仍会优先注入它的依赖项，而不会使用默认值。
 * 测试环境中：
 *
 * 在测试环境中，通常不会有 providesDemoAssetManager 这种生产环境下的依赖注入配置（或测试用例会有单独的模块提供替代实现）。
 * 如果在测试时没有通过依赖注入提供 DemoAssetManager，Kotlin 的默认参数 JvmUnitTestDemoAssetManager 就会被使用。
 * 这确保了在测试环境下，DemoNiaNetworkDataSource 依赖于 JvmUnitTestDemoAssetManager 而不是生产环境的实现，从而实现与生产环境相隔离的测试逻辑。
 * 4. 为什么能实现这种功能
 * 默认参数结合依赖注入：Kotlin 的默认参数机制使得即使没有通过依赖注入提供某个参数，类仍然能够正常构建实例。然而，当使用 Dagger Hilt 时，它会优先注入它找到的依赖，从而覆盖默认值。
 * 测试与生产环境的分离：通过这种方式，可以为生产环境和测试环境分别提供不同的实现，确保代码在不同环境下的行为符合预期。
 * 5. 总结
 * 生产环境：Hilt 会注入 providesDemoAssetManager 提供的 DemoAssetManager 实现，覆盖掉默认值 JvmUnitTestDemoAssetManager。
 * 测试环境：如果没有通过 Hilt 注入 DemoAssetManager，Kotlin 的默认参数 JvmUnitTestDemoAssetManager 将被使用。这种设计允许在不影响生产代码的情况下，轻松地进行测试。
 *
 */


/**
 * Converts a list of [T] to change list of all the items in it where [idGetter] defines the
 * [NetworkChangeList.id]
 */
private fun <T> List<T>.mapToChangeList(
    idGetter: (T) -> String,
) = mapIndexed { index, item ->
    NetworkChangeList(
        id = idGetter(item),
        changeListVersion = index,
        isDelete = false,
    )
}
