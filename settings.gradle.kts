rootProject.name = "Taklamakan"
pluginManagement {
    repositories {
        maven("https://repo.leavesmc.org/releases") {
            name = "leavesmc-releases"
        }
        maven("https://repo.leavesmc.org/snapshots") {
            name = "leavesmc-snapshots"
        }
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "2.3.0"
    }
}