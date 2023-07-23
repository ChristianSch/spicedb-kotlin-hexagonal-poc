/*
 * This convention plugin handles kotlin conventions.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // see java conventions plugin, needs to be the same
        jvmTarget = JavaVersion.toString()
        // Will retain parameter names for Java reflection
        javaParameters = true
    }
}