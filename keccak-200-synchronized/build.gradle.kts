plugins {
    id("java")
}

group = "com.babkovic"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":sponge-api"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.web.test)
}

tasks.test {
    useJUnitPlatform()
}