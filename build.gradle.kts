plugins {
    kotlin("jvm") version "1.9.23"
    id("maven-publish")
}

group = "nl.joozd.rosterparser"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "nl.joozd.rosterparser"
            artifactId = "rosterparser"
            version = "0.0.1"

            artifact(sourceJar.get()) // Attach the source JAR

            pom {
                licenses {
                    license {
                        name.set("The Affero General Public License, Version 3")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                        distribution.set("repo")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://joozd.nl/nexus/repository/maven-releases/")
            credentials {
                username = (findProperty("nexusUsername") ?: System.getenv("NEXUS_USERNAME") ?: "").toString()
                password = (findProperty("nexusPassword") ?: System.getenv("NEXUS_PASSWORD") ?: "").toString()
            }
        }
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}