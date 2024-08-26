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
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 *
 * 定义了一个用于委托其他 CoroutineWorker 执行工作的 DelegatingWorker，
 * 并通过 Hilt 依赖注入机制来创建这些工作者实例。代码还包含了用于设置工作者元数据和访问
 *
 * 接口定义与依赖注入：通过 HiltWorkerFactoryEntryPoint 接口来获取 HiltWorkerFactory，在运行时动态创建工作者实例。
 * 元数据添加：通过 delegatedData 函数，将目标 CoroutineWorker 的类名作为元数据存储在 WorkRequest 中，供 DelegatingWorker 使用。
 * 委托工作者：DelegatingWorker 负责将实际的工作委托给通过 HiltWorkerFactory 创建的 CoroutineWorker 实例，这使得库模块可以使用自定义的工作者，而无需控制 WorkManager 的全局配置。
 *
 */


/**
 * An entry point to retrieve the [HiltWorkerFactory] at runtime
 *
 * @EntryPoint：标注接口作为 Hilt 依赖注入系统的入口点，允许在应用运行时获取依赖。
 *
 * @InstallIn(SingletonComponent::class)：指定这个入口点在 Hilt
 * 的 SingletonComponent 组件中进行安装，这意味着它在应用的整个生命周期中是单例的。
 *
 * interface HiltWorkerFactoryEntryPoint：定义一个接口，作为获取 HiltWorkerFactory 的入口。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    /**
     * fun hiltWorkerFactory(): HiltWorkerFactory：定义一个方法，返回 HiltWorkerFactory，
     * 这个工厂用于创建带有依赖注入的 CoroutineWorker 实例。
     */
    fun hiltWorkerFactory(): HiltWorkerFactory
}

/**
 * private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"：
 * 定义一个私有常量，用于在 WorkRequest 的元数据中存储工作者类的名称。这个常量值将用于标识委托的 CoroutineWorker 类。
 */
private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

/**
 * Adds metadata to a WorkRequest to identify what [CoroutineWorker] the [DelegatingWorker] should
 * delegate to
 *
 * internal fun KClass<out CoroutineWorker>.delegatedData()：这是一个扩展函数，
 * 扩展了 KClass<out CoroutineWorker> 类，用于为 WorkRequest 添加元数据。
 *
 *
 * KClass 是 Kotlin 中表示类的类引用类型，它能够提供关于该类的元数据，包括类的 qualifiedName（全限定名）。
 * delegatedData 扩展函数通过 KClass 获取了 CoroutineWorker 的类名，并将其存储到 Data 对象中，以便后续在 DelegatingWorker 中使用。
 *
 * 如果 delegatedData 方法直接定义在 CoroutineWorker 中，方法内部将无法访问类的元数据，
 * 比如获取类名等信息。通过使用 KClass，你可以动态地获取并处理类的元数据，这在反射、依赖注入等场景中非常重要。
 *
 */
internal fun KClass<out CoroutineWorker>.delegatedData() =

    /**
     * Data.Builder()：创建一个新的 Data.Builder 实例，用于构建 WorkRequest 的 Data 对象。
     */
    Data.Builder()
        /**
         * .putString(WORKER_CLASS_NAME, qualifiedName)：
         * 将工作者类的 qualifiedName（全限定名）作为字符串添加到 Data 对象中，使用 WORKER_CLASS_NAME 作为键。
         */
        .putString(WORKER_CLASS_NAME, qualifiedName)
        /**
        .build()：构建并返回 Data 对象，包含了工作者类的名称。这段元数据稍后会被 DelegatingWorker 用于确定需要委托的实际 CoroutineWorker 实例。
         */
        .build()

/**
 * A worker that delegates sync to another [CoroutineWorker] constructed with a [HiltWorkerFactory].
 *
 * This allows for creating and using [CoroutineWorker] instances with extended arguments
 * without having to provide a custom WorkManager configuration that the app module needs to utilize.
 *
 * In other words, it allows for custom workers in a library module without having to own
 * configuration of the WorkManager singleton.
 *
 *
 * class DelegatingWorker(appContext: Context, workerParams: WorkerParameters)：定义一个 DelegatingWorker 类，它继承自 CoroutineWorker，接受 Context 和 WorkerParameters 作为构造参数。
 *
 *
 *
 *
 */
class DelegatingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    /**
     * private val workerClassName = workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""：
     * 从传入的 workerParams.inputData 中获取工作者类的名称。如果获取不到名称，则默认使用空字符串。
     */
    private val workerClassName =
        workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

    /**
     * private val delegateWorker = ...：使用依赖注入框架 Hilt 通过 EntryPointAccessors
     * 获取 HiltWorkerFactory 实例，并创建指定类名的 CoroutineWorker 实例。
     */
    private val delegateWorker =
        /**
         * EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)：
         * 通过 EntryPointAccessors 获取 HiltWorkerFactoryEntryPoint 实例。
         */
        EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
            /**
             * .hiltWorkerFactory()：调用 HiltWorkerFactoryEntryPoint 接口中的 hiltWorkerFactory() 方法，获取 HiltWorkerFactory 实例。
             */
            .hiltWorkerFactory()
            /**
             * .createWorker(appContext, workerClassName, workerParams)：使用 HiltWorkerFactory 创建工作者实例，传入上下文、工作者类名以及工作者参数。
             */
            .createWorker(appContext, workerClassName, workerParams)
            /**
             * as? CoroutineWorker：将创建的实例转换为 CoroutineWorker 类型。
             */
            as? CoroutineWorker
        /**
         * ?: throw IllegalArgumentException("Unable to find appropriate worker")：如果无法创建合适的工作者实例，则抛出异常。
         */
            ?: throw IllegalArgumentException("Unable to find appropriate worker")

    /**
     *
     * override suspend fun getForegroundInfo(): ForegroundInfo = delegateWorker.getForegroundInfo()：重写 getForegroundInfo 方法，将调用委托给实际的工作者实例，返回前台服务的 ForegroundInfo。
     */
    override suspend fun getForegroundInfo(): ForegroundInfo =
        delegateWorker.getForegroundInfo()

    /**
     * override suspend fun doWork(): Result = delegateWorker.doWork()：重写 doWork 方法，将任务执行逻辑委托给实际的工作者实例，返回执行结果
     */
    override suspend fun doWork(): Result =
        delegateWorker.doWork()
}

/**
 *
 *
 *
 *
 *
 * Kotlin 中的 KClass
 * 作用
 * KClass 是 Kotlin 中用于表示 Kotlin 类的类引用的类型。它与 Java 中的 Class 类似，但专门用于 Kotlin 类型。通过 KClass，你可以获取类的元数据（如类的名称、构造函数、成员函数、属性等），并在运行时操作这些类。
 *
 * 使用场景
 * 反射：可以使用 KClass 来获取类的元数据并通过反射调用类的构造函数或方法。
 *
 * kotlin
 * Copy code
 * val kClass: KClass<MyClass> = MyClass::class
 * val constructors = kClass.constructors // 获取所有构造函数
 * 依赖注入：在依赖注入框架中，可以使用 KClass 作为参数来获取特定类型的实例。
 *
 * kotlin
 * Copy code
 * fun <T : Any> inject(clazz: KClass<T>): T {
 *     // 根据 KClass 获取实例
 * }
 * 序列化与反序列化：在序列化框架（如 Kotlinx.serialization）中，KClass 可以用来动态确定序列化类型。
 *
 * kotlin
 * Copy code
 * val serializer = serializer(MyClass::class)
 * 动态类型操作：在泛型中使用 KClass，可以在运行时确定泛型类型，并进行相应的操作。
 *
 * kotlin
 * Copy code
 * fun <T : Any> createInstance(kClass: KClass<T>): T? {
 *     return kClass.primaryConstructor?.call()
 * }
 * Java 中的对应概念
 * 在 Java 中，KClass 的对应概念是 Class。
 *
 * 使用 Class
 * 反射：
 *
 * java
 * Copy code
 * Class<MyClass> clazz = MyClass.class;
 * Constructor<?>[] constructors = clazz.getConstructors(); // 获取所有构造函数
 * 依赖注入：
 *
 * java
 * Copy code
 * public <T> T inject(Class<T> clazz) {
 *     // 根据 Class 获取实例
 * }
 * 序列化与反序列化：
 *
 * java
 * Copy code
 * public <T> Serializer<T> getSerializer(Class<T> clazz) {
 *     // 使用 Class 动态确定序列化器
 * }
 * 动态类型操作：
 *
 * java
 * Copy code
 * public <T> T createInstance(Class<T> clazz) throws Exception {
 *     return clazz.getDeclaredConstructor().newInstance();
 * }
 * Dart 中的对应概念
 * 在 Dart 中，没有直接与 KClass 或 Java Class 等效的内建类型，但 Dart 的 Type 可以表示对象的运行时类型。Dart 不支持直接反射操作（如获取类的构造函数或成员），但可以通过一些库（如 dart:mirrors）来实现类似功能。
 *
 * 使用 Type
 * 类型检查与比较：
 *
 * dart
 * Copy code
 * void checkType(Object obj) {
 *     if (obj.runtimeType == MyClass) {
 *         print('Object is of type MyClass');
 *     }
 * }
 * 简单反射（通过 dart:mirrors）：
 *
 * dart
 * Copy code
 * import 'dart:mirrors';
 *
 * void reflectExample() {
 *     ClassMirror classMirror = reflectClass(MyClass);
 *     var constructors = classMirror.declarations.values
 *         .whereType<MethodMirror>()
 *         .where((m) => m.isConstructor); // 获取所有构造函数
 * }
 * 总结
 * Kotlin 的 KClass 是一个强大且灵活的工具，用于在运行时获取和操作类的元数据，类似于 Java 的 Class。
 * Java 中，Class 是 KClass 的直接对应物，用于反射、依赖注入、动态操作等。
 * Dart 没有完全等效的概念，但可以使用 Type 进行类型检查，并通过 dart:mirrors 库实现部分反射功能。
 * KClass 是 Kotlin 特有的，可以处理 Kotlin 的特性（如扩展函数、协程等），使其在 Kotlin 开发中尤为有用。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/**
 *
 *
 *
 * 1. 扩展函数简介
 * 扩展函数允许你向现有的类添加新的函数，而无需继承该类或使用装饰器模式。这在Kotlin中是一个强大的特性，能够提高代码的可读性和可维护性。
 *
 * 定义扩展函数的语法：
 *
 * kotlin
 * Copy code
 * fun ReceiverType.functionName(params): ReturnType {
 *     // function body
 * }
 * ReceiverType：你想要扩展的类型。
 * functionName：新添加的函数名称。
 * params：函数参数。
 * ReturnType：函数返回类型。
 * 当你定义了一个扩展函数后，任何该类型的实例都可以直接调用这个函数，就像它是类型本身的方法一样。
 *
 * 2. 你的扩展函数解析
 * 你的扩展函数定义：
 *
 * kotlin
 * Copy code
 * fun KClass<out CoroutineWorker>.delegatedData() =
 *     Data.Builder()
 *         .putString(WORKER_CLASS_NAME, qualifiedName)
 *         .build()
 * 解析：
 *
 * ReceiverType：KClass<out CoroutineWorker>
 * 这意味着你为所有CoroutineWorker子类的KClass类型添加了一个名为delegatedData()的扩展函数。
 * function body：创建并返回一个包含WORKER_CLASS_NAME的Data对象。
 * 关键点：
 *
 * 任何CoroutineWorker子类对应的KClass实例都可以调用delegatedData()函数。
 * 3. 类型层次结构和泛型协变
 * 假设：
 *
 * 你有一个SyncWorker类，继承自CoroutineWorker：
 * kotlin
 * Copy code
 * class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
 *     override suspend fun doWork(): Result {
 *         // 工作逻辑
 *     }
 * }
 * KClass和协变：
 *
 * 在Kotlin中，泛型类型参数可以声明为协变（covariant），使用out关键字。
 * 协变允许你在类型层次结构中安全地向上转换类型。
 * 示例：
 *
 * KClass<SyncWorker>是KClass<out CoroutineWorker>的一个子类型，因为SyncWorker是CoroutineWorker的子类。
 * 因此，你可以将KClass<SyncWorker>视为KClass<out CoroutineWorker>，这使得扩展函数delegatedData()可用。
 * 代码示例：
 *
 * kotlin
 * Copy code
 * val syncWorkerClass: KClass<SyncWorker> = SyncWorker::class
 * val data = syncWorkerClass.delegatedData() // 调用成功
 * 4. 代码中的具体应用
 * 你的代码片段：
 *
 * kotlin
 * Copy code
 * fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
 *     .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
 *     .setConstraints(SyncConstraints)
 *     .setInputData(SyncWorker::class.delegatedData())
 *     .build()
 * 解析步骤：
 *
 * SyncWorker::class
 *
 * 获取SyncWorker类的Kotlin反射对象，类型为KClass<SyncWorker>。
 * 调用delegatedData()
 *
 * 因为SyncWorker继承自CoroutineWorker，所以KClass<SyncWorker>可以被视为KClass<out CoroutineWorker>。
 * 因此，SyncWorker::class可以调用扩展函数delegatedData()，返回一个Data对象。
 * setInputData()
 *
 * 将生成的Data对象设置为WorkRequest的输入数据。
 * 完整流程：
 *
 * 通过调用delegatedData()，你为WorkRequest准备了特定的输入数据，该数据包含了SyncWorker类的完全限定名。
 * DelegatingWorker可以使用这些数据来动态创建或管理SyncWorker的实例，具体取决于你的实现。
 * DelegatingWorker的可能用途：
 *
 * kotlin
 * Copy code
 * class DelegatingWorker(
 *     appContext: Context,
 *     workerParams: WorkerParameters,
 * ) : CoroutineWorker(appContext, workerParams) {
 *
 *     override suspend fun doWork(): Result {
 *         val workerClassName = inputData.getString(WORKER_CLASS_NAME)
 *         val workerClass = Class.forName(workerClassName).kotlin
 *         val workerInstance = workerClass.primaryConstructor!!.call(appContext, workerParams) as CoroutineWorker
 *         return workerInstance.doWork()
 *     }
 * }
 * 解释：
 *
 * DelegatingWorker从输入数据中获取需要实际执行的Worker类名。
 * 通过反射创建该Worker的实例并调用其doWork()方法。
 * 这种设计模式允许你创建一个通用的Worker来委托实际的工作给不同的Worker实现。
 * 5. 关于文件位置的说明
 * 同一文件中的关系：
 *
 * 虽然DelegatingWorker和delegatedData()定义在同一个文件中，但这并不是SyncWorker::class能够调用delegatedData()的原因。
 * 关键在于类型兼容性和扩展函数的可见性。
 * 只要扩展函数在当前作用域内可见（例如通过import语句导入），并且类型匹配，就可以被调用。
 * 可见性控制：
 *
 * 如果你希望限制扩展函数的可见性，可以使用internal或private修饰符。
 * kotlin
 * Copy code
 * internal fun KClass<out CoroutineWorker>.delegatedData() = // ...
 * 这将限制函数只能在特定模块或文件中可见。
 * 6. 总结
 * 扩展函数允许你为现有类型添加新功能，delegatedData()就是这样一个为KClass<out CoroutineWorker>添加的新函数。
 * 类型兼容性和泛型协变确保了SyncWorker::class能够调用delegatedData()，因为SyncWorker是CoroutineWorker的子类。
 * 文件位置并不影响扩展函数的调用，只要函数在当前作用域内可见且类型匹配即可。
 * 希望这能更全面地帮助你理解代码中SyncWorker::class.delegatedData()能够成功调用的原因！如果你还有其他疑问，欢迎继续提问。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */