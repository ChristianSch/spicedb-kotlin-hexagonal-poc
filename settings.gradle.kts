rootProject.name = "spicedb-java-poc"

include(":domain")
include(":app")
include(":spicedb-authz")

// from gradle.properties
val kotlinVersion: String by settings

dependencyResolutionManagement {
    @Suppress("UnstablelibraryUsage", "UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            library("kotlin-stdlib", "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        }
    }
}