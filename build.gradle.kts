plugins {
    id("java")
}

java {
    withJavadocJar()
    withSourcesJar()
}

group = "io.github.destroyerofcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        setUrl("https://nexus.zentity.com/repository/maven-public/");
    }
}

dependencies {
    implementation(project(":keccak-200-168"))
    implementation(project(":keccak-1600-256"))
    implementation(project(":sponge-api"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)

}

tasks.test {
    useJUnitPlatform {
        excludeTags("performanceHeavy")
    }
}