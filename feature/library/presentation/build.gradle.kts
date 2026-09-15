plugins {
    alias(libs.plugins.vibeplayer.android.feature)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.library.presentation"
}

dependencies {
    implementation(projects.feature.library.domain)

    testImplementation(projects.core.testing)
}
