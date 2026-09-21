plugins {
    alias(libs.plugins.vibeplayer.android.feature)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.library.presentation"
}

dependencies {
    implementation(projects.feature.library.domain)

    implementation(libs.reorderable)

    testImplementation(projects.core.testing)
}
