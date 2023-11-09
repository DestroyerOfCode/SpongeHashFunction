plugins {
    id("java")
}

group = "com.babkovic"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":keccak-200-synchronized"))
    implementation(project(":sponge-api"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform {
        excludeTags("performanceHeavy")
    }
}