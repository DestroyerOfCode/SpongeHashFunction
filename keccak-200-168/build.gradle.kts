plugins {
    id("java")
}

group = "io.github.destroyerofcode"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":sponge-api"))

    // tests
    testImplementation(project(path = ":sponge-api", configuration = "testArtifacts"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.web.test)
}

tasks.test {
    useJUnitPlatform()
}