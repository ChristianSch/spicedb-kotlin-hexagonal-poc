plugins {
    id("do24.java-conventions")
    id("do24.kotlin-conventions")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":spicedb-authz"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}