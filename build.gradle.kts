plugins {
    id("java")
    `maven-publish`
}

group = "com.babkovic"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        setUrl("https://nexus.zentity.com/repository/maven-public/");
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.babkovic"
            artifactId = "keccak"
            version = "1.0-SNAPSHOT"
        }
    }
    repositories {
        maven {
            name = "sponge-hash"
            url = uri("https://nexus.zentity.com/repository/maven-public/")

            credentials {
                username = project.properties["repoUser"] as String
                password = project.properties["repoPassword"] as String
            }
        }
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