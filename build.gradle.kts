import java.net.URL

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka") version "1.9.20"
    id("maven-publish")
}

group = "nl.joozd.rosterparser"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation ("com.itextpdf:itextg:5.5.10") // iText PDF for PDF parsing. This requires AGPL License.
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta") // Kotlin Coroutines, not used at the moment
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "nl.joozd.rosterparser"
            artifactId = "rosterparser"
            version = "0.0.1"

            artifact(sourceJar.get()) // Attach the source JAR

            // Attach the Dokka HTML documentation JAR
            artifact(tasks.named("dokkaHtmlJar").get())

            // Attach the Dokka Javadoc JAR
            artifact(tasks.named("dokkaJavadocJar").get())


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

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("docs"))

    dokkaSourceSets {
        named("main") {
            // Set module and package options as needed
            moduleName.set("RosterParser")
            includes.from("Module.md")
//            includeNonPublic.set(false)
//            skipEmptyPackages.set(true)
            reportUndocumented.set(true) // Warn if something is not documented
            jdkVersion.set(11) // Target JDK version
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(
                    URL("https://github.com/Joozd/rosterparser/" +
                        "tree/master/src/main/kotlin"
                )
                )
                remoteLineSuffix.set("#L")
            }
        }
    }
}



tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}