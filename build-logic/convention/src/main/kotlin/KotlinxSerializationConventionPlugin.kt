import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.library
import com.rfcoding.vibeplayer.convention.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KotlinxSerializationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-serialization"))

            dependencies {
                "implementation"(libs.library("kotlinx-serialization-json"))
            }
        }
    }
}
