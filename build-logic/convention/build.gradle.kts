plugins {
    `kotlin-dsl`
}

group = "com.rfcoding.vibeplayer.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.vibeplayer.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.vibeplayer.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.vibeplayer.android.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("domainModule") {
            id = libs.plugins.vibeplayer.domain.module.get().pluginId
            implementationClass = "DomainModuleConventionPlugin"
        }
        register("compose") {
            id = libs.plugins.vibeplayer.compose.get().pluginId
            implementationClass = "ComposeConventionPlugin"
        }
        register("koin") {
            id = libs.plugins.vibeplayer.koin.get().pluginId
            implementationClass = "KoinConventionPlugin"
        }
        register("room") {
            id = libs.plugins.vibeplayer.room.get().pluginId
            implementationClass = "RoomConventionPlugin"
        }
        register("kotlinxSerialization") {
            id = libs.plugins.vibeplayer.kotlinx.serialization.get().pluginId
            implementationClass = "KotlinxSerializationConventionPlugin"
        }
    }
}
