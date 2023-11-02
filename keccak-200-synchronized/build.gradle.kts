plugins {
    id("java")
}

group = "com.babkovic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":sponge-api"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.6.0")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+HeapDumpOnOutOfMemoryError")
}