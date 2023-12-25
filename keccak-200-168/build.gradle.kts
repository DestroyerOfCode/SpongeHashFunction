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
    testImplementation(project(path = ":sponge-api", configuration = "testArtifacts"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.web.test)
}

tasks.test {
    useJUnitPlatform()
}