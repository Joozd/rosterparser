import java.net.URI

plugins {
    kotlin("jvm") version "2.4.20"
    id("org.jetbrains.dokka") version "2.2.0"
    id("org.jetbrains.dokka-javadoc") version "2.2.0"
    id("maven-publish")
}

val versionName = "0.1.9-beta"
val groupID = "nl.joozd.rosterparser"

group = groupID
version = versionName

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    // iText PDF parsing. Requires AGPL.
    // This EOL Android-compatible version is intentionally retained.
    implementation("com.itextpdf:itextg:5.5.10")

    // Apache Commons CSV parsing.
    implementation("org.apache.commons:commons-csv:1.14.1")

    // ICU4J, used for text encoding detection.
    implementation("com.ibm.icu:icu4j:78.3")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

/**
 * Dokka configuration.
 */
dokka {
    dokkaPublications.html {
        moduleName.set("RosterParser")
        outputDirectory.set(layout.buildDirectory.dir("docs"))
    }

    dokkaPublications.javadoc {
        moduleName.set("RosterParser")
        outputDirectory.set(layout.buildDirectory.dir("javadoc"))
    }

    dokkaSourceSets.main {
        includes.from("Module.md")

        reportUndocumented.set(true)
        jdkVersion.set(21)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/Joozd/rosterparser/" +
                            "tree/master/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}

/**
 * Packages the main source files into a sources JAR.
 */
val sourceJar = tasks.register<Jar>("sourceJar") {
    description = "Packages the main source files into a sources JAR."
    group = "build"

    archiveClassifier.set("sources")
    from(sourceSets.named("main").map { it.allSource })
}

/**
 * Packages the generated Dokka HTML documentation into a JAR.
 */
val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
    description = "Packages the generated Dokka HTML documentation into a JAR."
    group = "documentation"

    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
    archiveClassifier.set("html-docs")
    from(layout.buildDirectory.dir("docs"))
}

/**
 * Packages the generated Dokka Javadoc documentation into a JAR.
 */
val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    description = "Packages the generated Dokka Javadoc documentation into a JAR."
    group = "documentation"

    dependsOn(tasks.named("dokkaGeneratePublicationJavadoc"))
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("javadoc"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = groupID
            artifactId = "rosterparser"
            version = versionName

            artifact(sourceJar)
            artifact(dokkaHtmlJar)
            artifact(dokkaJavadocJar)

            pom {
                name.set("RosterParser")
                description.set("Parser for airline roster data.")
                url.set("https://github.com/Joozd/rosterparser")

                licenses {
                    license {
                        name.set("GNU Affero General Public License, Version 3")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                        distribution.set("repo")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "reposilite"

            url = uri(
                if (versionName.endsWith("-SNAPSHOT")) {
                    "https://repo.joozd.nl/snapshots"
                } else {
                    "https://repo.joozd.nl/releases"
                }
            )

            credentials {
                username = findProperty("repoUsername")?.toString()
                    ?: error("Missing Gradle property: repoUsername")
                password = findProperty("repoPassword")?.toString()
                    ?: error("Missing Gradle property: repoPassword")
            }
        }
    }
}