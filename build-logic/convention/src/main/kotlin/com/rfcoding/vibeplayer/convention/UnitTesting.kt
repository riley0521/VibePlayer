package com.rfcoding.vibeplayer.convention

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * JUnit5 (Jupiter) + AssertK + Turbine + coroutines-test for every module's unit tests.
 */
internal fun Project.configureUnitTesting() {
    dependencies {
        "testImplementation"(platform(libs.library("junit-bom")))
        "testImplementation"(libs.bundle("unit-testing"))
        "testRuntimeOnly"(libs.library("junit-platform-launcher"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
