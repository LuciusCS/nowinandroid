


在 root project 中 include 这个 build-logic 项目， 需要在根目录下的settings.gradle.kts进行引用
因为复合构建会把项目里的构建配置包含进来，所以我们不能直接使用 include(“:build-logic”)，
而是要使用 includeBuild(“build-logic”)

