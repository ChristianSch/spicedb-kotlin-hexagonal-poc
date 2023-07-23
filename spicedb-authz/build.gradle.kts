import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("do24.java-conventions")
    id("do24.kotlin-conventions")
}

version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("com.authzed.api:authzed:0.5.0")
    implementation("io.grpc:grpc-protobuf:1.54.1")
    implementation("io.grpc:grpc-stub:1.54.1")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.testcontainers:testcontainers:1.18.3")
    testImplementation("org.testcontainers:junit-jupiter:1.18.3")
    testImplementation("com.willowtreeapps.assertk:assertk:0.26.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}