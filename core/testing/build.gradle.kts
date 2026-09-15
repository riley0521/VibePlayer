plugins {
    alias(libs.plugins.vibeplayer.domain.module)
}

// Fakes of the :core:domain interfaces, shared by every module's unit tests (testImplementation only).
dependencies {
    api(projects.core.domain)
    api(libs.kotlinx.coroutines.core)
}
