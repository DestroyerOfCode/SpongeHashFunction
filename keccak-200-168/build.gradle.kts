plugins {
    id("java")
    id("signing")
    `maven-publish`
}

group = "io.github.destroyerofcode"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
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

tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

val isReleaseVersion: Boolean = version.toString().endsWith("SNAPSHOT").let { !it }

publishing {
    repositories {
        maven {
            val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

            name = "OSSRH"
            url = uri(
                if (isReleaseVersion) releaseRepo
                else snapshotRepo
            )

            credentials {
                username = findProperty("repoUser").toString()
                password = findProperty("repoPassword").toString()
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.github.destroyerofcode"
            artifactId = "keccak-200-168"
            version = this.version

            pom {
                name.set("sponge-hash")
                description.set("A hashing algorithm based on sponge structure")
                url.set("https://github.com/DestroyerOfCode/SpongeHashFunction")

                licenses {
                    license {
                        name.set("The Unlicense")
                        url.set("https://unlicense.oeg/")
                    }
                }
                scm {
                    url.set("https://github.com/DestroyerOfCode/SpongeHashFunction")
                    connection.set("scm:git@github.com:DestroyerOfCode/SpongeHashFunction.git")
                    developerConnection.set("scm:git@github.com:DestroyerOfCode/SpongeHashFunction.git")
                }
                developers {
                    developer {
                        id.set("spongehash")
                        name.set("Marius Babkovic")
                        email.set("marius.babkovic9@gmail.com")
                    }
                }
            }
        }
    }
}

configure<SigningExtension> {
    findProperty("signing.keyId")?.toString()?.let { keyId ->
        findProperty("signing.password")?.toString()?.let { password ->
            useInMemoryPgpKeys(keyId, password)
            sign(publishing.publications["mavenJava"])
        }
    }
}
tasks.withType<Sign> {
    onlyIf { isReleaseVersion }
}