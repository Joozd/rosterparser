plugins {
    kotlin("jvm") version "1.9.23"
    id("maven-publish")
}

group = "nl.joozd.rosterparser"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "nl.joozd.rosterparser"
            artifactId = "rosterparser"
            version = "0.0.1"
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