import androidx.room.gradle.RoomExtension
import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.library
import com.rfcoding.vibeplayer.convention.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("room"))
            pluginManager.apply(libs.pluginId("ksp"))

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                "implementation"(libs.library("androidx-room-runtime"))
                "implementation"(libs.library("androidx-room-ktx"))
                "ksp"(libs.library("androidx-room-compiler"))
            }
        }
    }
}
