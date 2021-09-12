plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Versions.kotlin
    id("default-java-publish")
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(kotlin("stdlib"))
    api(rootProject)
    api(Dependencies.serializationCore)
    api(Dependencies.serializationJson)
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.shouldko)
}
