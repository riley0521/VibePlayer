plugins {
    alias(libs.plugins.vibeplayer.android.feature)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.player.presentation"
}

dependencies {
    testImplementation(projects.core.testing)
}
