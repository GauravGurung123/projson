plugins {
    kotlin("jvm") version "2.3.10"
}

group = "org.gojson"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}