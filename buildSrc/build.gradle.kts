plugins {
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

// we need to add any external dependencies of the convention plugins here, as "precompiled scripts must not include a version number"
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
}