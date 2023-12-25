plugins {
    id("java")
}

group = "com.babkovic"
version = "1.0"

repositories {
    mavenCentral()
}
val testArtifacts by configurations.creating

tasks.register<Jar>("testJar") {
    archiveClassifier.set("tests")
    from(sourceSets["test"].output)
}

artifacts {
    add(testArtifacts.name, tasks["testJar"])
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
