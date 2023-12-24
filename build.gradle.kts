plugins {
    id("java")
}

group = "com.babkovic"
version = "1.0"

repositories {
    mavenCentral()
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