plugins {
    id("do24.java-conventions")
    id("do24.kotlin-conventions")
}

dependencies {
    // NOTE: it's important not to import anything other than testing utilities
    // here. This is raw domain code, and we want to keep it as clean as possible.
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}