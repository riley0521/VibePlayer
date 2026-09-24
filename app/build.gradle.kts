plugins {
    alias(libs.plugins.vibeplayer.android.application)
    alias(libs.plugins.vibeplayer.compose)
    alias(libs.plugins.vibeplayer.koin)
    alias(libs.plugins.vibeplayer.kotlinx.serialization)
}

android {
    namespace = "com.rfcoding.vibeplayer"

    defaultConfig {
        // yt-dlp ships Python and FFmpeg per ABI; phones are arm64 and emulators x86_64.
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    packaging {
        // youtubedl-android runs its binaries from the extracted native library folder.
        jniLibs.useLegacyPackaging = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }
}

dependencies {
    // Core
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.player)
    implementation(projects.core.presentation)
    implementation(projects.core.designSystem)

    // Features
    implementation(projects.feature.permission.presentation)
    implementation(projects.feature.library.domain)
    implementation(projects.feature.library.data)
    implementation(projects.feature.library.presentation)
    implementation(projects.feature.player.presentation)
    implementation(projects.feature.downloader.domain)
    implementation(projects.feature.downloader.data)
    implementation(projects.feature.downloader.presentation)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.androidx.workmanager)
}
