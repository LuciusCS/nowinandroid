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

package com.google.samples.apps.nowinandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.metrics.performance.JankStats
import com.google.samples.apps.nowinandroid.MainActivityUiState.Loading
import com.google.samples.apps.nowinandroid.MainActivityUiState.Success
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsHelper
import com.google.samples.apps.nowinandroid.core.analytics.LocalAnalyticsHelper
import com.google.samples.apps.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.data.util.NetworkMonitor
import com.google.samples.apps.nowinandroid.core.data.util.TimeZoneMonitor
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand
import com.google.samples.apps.nowinandroid.core.ui.LocalTimeZone
import com.google.samples.apps.nowinandroid.ui.NiaApp
import com.google.samples.apps.nowinandroid.ui.rememberNiaAppState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainActivity"

/**
 *
 *
 * ComponentActivity 是 Android Jetpack 架构组件库中一个重要的基类，它继承自 Activity，并且提供了与 Jetpack 组件
 * （例如 ViewModel、LiveData、SavedStateRegistry 和 Lifecycle）无缝集成的功能。ComponentActivity 是许多现代 Android 应用中推荐使用的基础活动类。
 *
 * 作用与特点
 * Jetpack 组件集成:
 * ComponentActivity 内置了对 Jetpack 架构组件的支持，包括 ViewModel、LiveData 和 SavedStateRegistry，使得这些组件可以更容易地与 Activity 生命周期结合使用。
 * 例如，你可以轻松地在 ComponentActivity 中使用 ViewModelProvider 来获取 ViewModel，并且这些 ViewModel 可以自动管理其生命周期，与 Activity 的生命周期同步。
 * Lifecycle 支持:
 * ComponentActivity 实现了 LifecycleOwner 接口，因此它可以很好地与生命周期感知组件集成。这意味着你可以直接在 ComponentActivity 中使用 LifecycleObserver，来响应 Activity 生命周期的不同阶段。
 * SavedStateRegistry 支持:
 * ComponentActivity 提供了 SavedStateRegistry 功能，它允许你在 Activity 被销毁和重建时保存和恢复数据。相比传统的 onSaveInstanceState 方法，这种方式更加灵活和强大，适合用于保存需要长期保持的数据。
 * OnBackPressedDispatcher:
 * ComponentActivity 包含一个 OnBackPressedDispatcher，它允许你管理后退按钮的行为。你可以注册多个回调，并且根据优先级处理 onBackPressed 事件。
 * ActivityResultRegistry 支持:
 * ComponentActivity 提供了 ActivityResultRegistry，它可以帮助你轻松管理 startActivityForResult 和其他返回结果的 API。相比传统的 startActivityForResult 方法，ActivityResultRegistry 更加灵活，并且可以处理更复杂的回调场景。
 * 使用场景
 * 现代 Android 开发: 如果你正在使用 Jetpack 组件构建 Android 应用，ComponentActivity 是一个很好的选择。它提供了更强大的生命周期管理、更方便的数据保存机制，并且更易于与其他 Jetpack 组件集成。
 * 替代传统的 Activity: 对于大多数场景，ComponentActivity 是对 Activity 的一个更现代化的替代品，提供了更好的可维护性和可扩展性。
 * 示例代码
 * kotlin
 * Copy code
 * class MyActivity : ComponentActivity() {
 *
 *     private lateinit var viewModel: MyViewModel
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         // 使用 ViewModelProvider 获取 ViewModel
 *         viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
 *
 *         // 观察 LiveData
 *         viewModel.data.observe(this, Observer { data ->
 *             // 更新 UI
 *         })
 *
 *         // 设置 OnBackPressedDispatcher
 *         onBackPressedDispatcher.addCallback(this) {
 *             // 自定义返回行为
 *         }
 *     }
 * }
 * 在这个例子中，MyActivity 继承自 ComponentActivity，它可以利用 ViewModel 和 LiveData 来管理数据，并且通过 onBackPressedDispatcher 自定义返回按钮行为。这些功能的集成使得 ComponentActivity 成为现代 Android 开发中的推荐选择。
 *
 *
 */

/**
 *
 * @AndroidEntryPoint：这是Dagger-Hilt的注解，表示该Activity是依赖注入的入口点。Hilt会在该类中自动生成所需的代码来进行依赖注入。
 * 的主要作用是将 Hilt 的依赖注入能力扩展到 Android 框架类（如 Activity、Fragment、Service 等）中。通过使用这个注解，你可以让 Hilt 自动地为这些类生成并注入依赖。
 *
 * 具体来说，@AndroidEntryPoint 会：
 *
 * 生成 Hilt 组件：它会生成一个 Hilt 组件，这个组件会在注解的类中提供所有必要的依赖。
 * 为 Android 框架类添加依赖注入：通过注解，Hilt 会自动地将依赖注入到相应的 Android 框架类中，而无需手动进行依赖注入的配置。
 *
 *
 * 继承关系：如果一个 Activity 或 Fragment 继承自另一个带有 @AndroidEntryPoint 的类，子类也必须加上 @AndroidEntryPoint 注解。
 * 唯一性：每个被 @AndroidEntryPoint 注解的类只能有一个构造函数，这个构造函数中不能有任何参数。
 *
 * @AndroidEntryPoint 通过简化依赖注入的方式，提高了代码的可维护性和可读性，尤其是在复杂的 Android 项目中，它显著减少了样板代码（boilerplate code）的编写。
 *
 *
 *
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Lazily inject [JankStats], which is used to track jank throughout the app.
     *
     * lazyStats：dagger.Lazy<JankStats>是Dagger提供的延迟初始化的包装类，
     * 允许你在需要的时候才创建JankStats实例。JankStats用于跟踪应用中的卡顿现象（jank）。
     *
     */
    @Inject
    lateinit var lazyStats: dagger.Lazy<JankStats>

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var timeZoneMonitor: TimeZoneMonitor

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var userNewsResourceRepository: UserNewsResourceRepository

    /**
     * viewModels()：这是Jetpack提供的委托，用于从ViewModelProvider中获取ViewModel实例。
     *
     * 在 Hilt 中，@AndroidEntryPoint 和 @HiltViewModel 的配合使用是为了简化依赖注入的流程，但在 Activity 或 Fragment 中获取 ViewModel 时，
     * 通常使用 by viewModels() 的方式而不是注解的方式。这主要与 ViewModel 的生命周期管理和 Hilt 的设计有关。
     *
     * 1. ViewModel 的生命周期管理
     * ViewModel 的主要设计目标之一是与 Activity 或 Fragment 的生命周期耦合。Android 提供了 ViewModelProvider
     * 来管理 ViewModel 的创建和存储，确保 ViewModel 的实例在配置变化（如屏幕旋转）时保持不变。
     *
     * 当你在 Activity 或 Fragment 中使用 by viewModels() 时，实际上是借助了 ViewModelProvider
     * 来确保 ViewModel 实例的正确创建和管理。这种方式可以保证 ViewModel 只在需要时被创建，并且在 Activity 或 Fragment 重新创建时不会丢失数据。
     *
     * 2. Hilt 与 ViewModel 的集成
     * Hilt 通过 @HiltViewModel 注解来提供 ViewModel 的依赖注入支持，但它并不会直接管理 ViewModel 的生命周期。
     * by viewModels() 是 Android 提供的一个委托属性，用于创建 ViewModel 并确保其与 Activity 或 Fragment 的生命周期绑定。
     *
     * 当你使用 by viewModels() 时，Hilt 自动参与了 ViewModel 的创建过程，并注入其依赖。它确保 ViewModel 的依赖通过构造函数被正确注入。
     *
     * 3. 为什么不直接用注解的方式
     * ViewModel 的创建涉及到生命周期和作用域的管理。直接用注解的方式（例如 @Inject）来注入 ViewModel 会绕过 ViewModelProvider
     * 的机制，从而失去 ViewModel 的生命周期管理功能。例如，无法确保在配置变化时，ViewModel 实例得以保留，而是会重新创建，这与 ViewModel 的设计初衷相违背。
     *
     * 4. 总结
     * 使用 by viewModels() 是为了利用 ViewModelProvider 提供的生命周期管理功能，而 Hilt 通过 @HiltViewModel 和构造函数注入来简化 ViewModel 的依赖管理。
     * 两者结合，既保证了 ViewModel 的正确注入，也保持了 ViewModel 的生命周期管理。
     *
     *
     */
    val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        ///启用启动屏幕，使应用在加载期间显示启动画面。
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        /**
         * 用mutableStateOf初始化的uiState变量用于跟踪UI的当前状态，默认值为Loading（表示加载中）
         *
         * 在 Kotlin 中，by 关键字可以用于委托属性，它允许将属性的访问和修改委托给另一个对象或函数。关于你的代码：
         *
         * kotlin
         * Copy code
         * var uiState: MainActivityUiState by mutableStateOf(Loading)
         * 这个示例使用了委托属性和 Jetpack Compose 中的 mutableStateOf，我们来详细解析。
         *
         * 1. 委托属性（Delegated Properties）和 by 关键字
         * Kotlin 的委托属性特性允许将属性的 get 和 set 操作委托给另一个对象或函数。通过 by 关键字，你可以将一个属性的实际读写操作委托给其他实现。
         *
         * 在这个例子中：
         *
         * kotlin
         * Copy code
         * var uiState: MainActivityUiState by mutableStateOf(Loading)
         * uiState 属性的读写操作被委托给 mutableStateOf(Loading) 返回的对象。
         * 这个对象（由 mutableStateOf 创建）管理 uiState 的存取，内部维护着这个状态的值。
         * 2. mutableStateOf 的作用
         * mutableStateOf 是 Jetpack Compose 中的一个函数，用于创建可观察的状态（State）对象。State 是 Compose 中一个核心概念，它允许你在 UI 组件中使用的状态发生变化时自动触发 UI 的重组（recomposition）。
         *
         * mutableStateOf 的具体作用：
         *
         * 状态存储：mutableStateOf 会返回一个 MutableState<T> 对象，这个对象持有类型为 T 的值。在这个例子中，T 是 MainActivityUiState。
         * 观察与更新：当 MutableState 对象的值发生变化时，所有读取该状态的 UI 组件都会自动重组，从而反映出状态的更新。
         * 例如：
         *
         * kotlin
         * Copy code
         * var uiState: MainActivityUiState by mutableStateOf(Loading)
         * 这里的 uiState 是一个 MutableState 对象，它被初始化为 Loading（假设 Loading 是 MainActivityUiState 的一个实例）。
         * 当 uiState 的值改变时，Jetpack Compose 将检测到这个变化，并自动重组依赖于 uiState 的 UI 组件，从而更新 UI。
         * 3. 总结
         * by 关键字用于委托属性的实现，将属性的存取操作委托给 mutableStateOf 返回的 MutableState 对象。
         * mutableStateOf 用于创建一个可变的、可观察的状态对象，当状态发生变化时，它会触发 UI 的自动重组，从而更新用户界面。
         * 这一机制使得在使用 Jetpack Compose 开发应用时，UI 能够根据状态的变化自动进行响应，简化了状态管理和 UI 更新的代码。
         *
         *
         */
        var uiState: MainActivityUiState by mutableStateOf(Loading)

        // Update the uiState
        /***
         * 启动一个协程在Lifecycle范围内工作，确保在Lifecycle.State.STARTED（Activity可见）时进行状态更新。
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                /**
                 * 从ViewModel中的uiState流收集数据，每次状态更新时都会更新uiState变量。
                 */

                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        // Keep the splash screen on-screen until the UI state is loaded. This condition is
        // evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
        // the UI.
        /**
         * 定义条件以决定何时关闭启动屏幕。当uiState为Loading时，启动屏幕保持显示；当uiState为Success时，启动屏幕消失。
         *
         * uiState初始化使用的 是 by mutableStateOf(Loading)，当它的值该改变时下面的代码就会被触发
         *
         */
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                Loading -> true
                is Success -> false
            }
        }

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations, and go edge-to-edge
        // This also sets up the initial system bar style based on the platform theme
        //设置Activity全屏显示，使内容可以延伸到系统栏（如状态栏和导航栏）区域。
        enableEdgeToEdge()

        /**
         * setContent { ... }：这是Jetpack Compose的入口点，定义Activity的UI内容。
         *
         */
        setContent {
            ///根据uiState判断是否使用暗色主题。
            val darkTheme = shouldUseDarkTheme(uiState)

            // Update the edge to edge configuration to match the theme
            // This is the same parameters as the default enableEdgeToEdge call, but we manually
            // resolve whether or not to show dark theme using uiState, since it can be different
            // than the configuration's dark theme value based on the user preference.
            /**
             * DisposableEffect(darkTheme) { ... }：根据主题配置更新Edge-to-Edge显示配置。
             * DisposableEffect用于在darkTheme变化时触发副作用，并在其不再需要时清理。
             *
             */
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }

            /***
             * rememberNiaAppState(...)：记住应用状态，结合网络监控器、新闻资源库、时区监控器等依赖项创建并维持应用状态。
             *
             */
            val appState = rememberNiaAppState(
                networkMonitor = networkMonitor,
                userNewsResourceRepository = userNewsResourceRepository,
                timeZoneMonitor = timeZoneMonitor,
            )

            /***
             * 从应用状态中收集当前时区的状态值，并在UI中使用。
             *
             */
            val currentTimeZone by appState.currentTimeZone.collectAsStateWithLifecycle()

            /**
             * 将analyticsHelper和currentTimeZone提供给Compose树中的子组件，使它们可以访问这些依赖项。
             */
            CompositionLocalProvider(
                LocalAnalyticsHelper provides analyticsHelper,
                LocalTimeZone provides currentTimeZone,
            ) {

                /**
                 * 应用自定义主题来包装子组件，支持暗色主题、安卓主题、以及动态主题的启用或禁用。
                 */
                NiaTheme(
                    darkTheme = darkTheme,
                    androidTheme = shouldUseAndroidTheme(uiState),
                    disableDynamicTheming = shouldDisableDynamicTheming(uiState),
                ) {

                    /***
                     * 应用的主要界面，根据appState显示内容。
                     */
                    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
                    NiaApp(appState)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lazyStats.get().isTrackingEnabled = true
    }

    override fun onPause() {
        super.onPause()
        lazyStats.get().isTrackingEnabled = false
    }
}

/**
 * Returns `true` if the Android theme should be used, as a function of the [uiState].
 */
@Composable
private fun shouldUseAndroidTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    Loading -> false
    is Success -> when (uiState.userData.themeBrand) {
        ThemeBrand.DEFAULT -> false
        ThemeBrand.ANDROID -> true
    }
}

/**
 * Returns `true` if the dynamic color is disabled, as a function of the [uiState].
 */
@Composable
private fun shouldDisableDynamicTheming(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    Loading -> false
    is Success -> !uiState.userData.useDynamicColor
}

/**
 * Returns `true` if dark theme should be used, as a function of the [uiState] and the
 * current system context.
 */
@Composable
private fun shouldUseDarkTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    Loading -> isSystemInDarkTheme()
    is Success -> when (uiState.userData.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

/**
 *
 *
 * 在 Android 开发中，@AndroidEntryPoint 是 Hilt 中非常独特的注解，用于简化依赖注入。不过，除了 Hilt 之外，还有其他一些注解或方法可以实现类似的功能，尽管它们的实现方式和应用场景可能有所不同。
 *
 * 1. @Inject
 * @Inject 是 Dagger 中的一个核心注解，尽管它本身不负责像 @AndroidEntryPoint 那样为整个 Android 组件生成依赖注入环境，但它是实现依赖注入的基础。在需要注入依赖的类中，标注需要注入的字段或构造函数即可。
 *
 * 示例：
 *
 * kotlin
 * Copy code
 * class MainViewModel @Inject constructor(
 *     private val repository: MyRepository
 * ) : ViewModel() {
 *     // ViewModel 逻辑
 * }
 * 2. @Component 和 @Subcomponent
 * Dagger 提供了 @Component 和 @Subcomponent 注解，用于定义依赖注入的组件。虽然这些注解不能直接替代 @AndroidEntryPoint 的功能，但它们允许手动设置依赖注入的组件树。
 *
 * 示例：
 *
 * kotlin
 * Copy code
 * @Component
 * interface AppComponent {
 *     fun inject(activity: MainActivity)
 * }
 *
 * class MainActivity : AppCompatActivity() {
 *     @Inject lateinit var myDependency: MyDependency
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         (applicationContext as MyApp).appComponent.inject(this)
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *         // 使用 myDependency
 *     }
 * }
 * 3. @ContributesAndroidInjector
 * 在 Dagger 2 中，@ContributesAndroidInjector 是用于简化 Android 组件（如 Activity、Fragment）的依赖注入的一种方式。它是通过 @Module 和 @ContributesAndroidInjector 来自动生成 Subcomponent，可以减少样板代码。
 *
 * 示例：
 *
 * kotlin
 * Copy code
 * @Module
 * abstract class MainActivityModule {
 *     @ContributesAndroidInjector
 *     abstract fun contributeMainActivity(): MainActivity
 * }
 * 这种方式需要在 AppComponent 中将 Module 引入。
 *
 * 4. Koin
 * Koin 是另一种依赖注入框架，与 Dagger 不同的是，它不需要注解，而是基于 DSL 来配置依赖注入。通过 startKoin 配置全局依赖，然后通过 by inject() 或者 get() 获取依赖。
 *
 * 示例：
 *
 * kotlin
 * Copy code
 * val appModule = module {
 *     viewModel { MainViewModel(get()) }
 *     single { MyRepository() }
 * }
 *
 * class MainActivity : AppCompatActivity() {
 *     private val viewModel: MainViewModel by inject()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *         // 使用 viewModel
 *     }
 * }
 * 总结
 * 虽然 @AndroidEntryPoint 是 Hilt 提供的一个非常便捷的注解，用于简化 Android 组件中的依赖注入，
 * 但你可以通过 Dagger 的 @Component、@Subcomponent、@ContributesAndroidInjector，甚至其他依赖注入框架如 Koin 来实现类似的功能。这些方法虽然不完全等同于 @AndroidEntryPoint，但它们各自有其适用场景和灵活性。
 *
 *
 *
 *
 */