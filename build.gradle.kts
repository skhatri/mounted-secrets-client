import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.tasks.bundling.Jar

buildscript {
    fun createBuildVersion(projectVersion: String): String {
        var derivedVersion = projectVersion
        val versionWithSnapshot = projectVersion.replace("-SNAPSHOT", "")
        val buildNumber = System.getenv("TRAVIS_BUILD_NUMBER") ?: "0"
        if (project.extra["release"] == "true") {
            derivedVersion = "${versionWithSnapshot}.${buildNumber}"
        } else {
            derivedVersion = "${versionWithSnapshot}.${buildNumber}-SNAPSHOT"
        }
        println("effective project version: ${derivedVersion}")
        return derivedVersion
    }
    project.version = createBuildVersion("${project.version}")
}

plugins {
    idea
    java
    jacoco
    id("org.sonarqube") version "2.8"
    id("maven-publish")
    maven
    signing

}

repositories {
  mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

dependencies {

    implementation("org.yaml:snakeyaml:1.26")
    testImplementation("org.mockito:mockito-core:3.3.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.6.1")

    testImplementation("org.junit.platform:junit-platform-commons:1.6.1")
    testImplementation("org.junit.platform:junit-platform-runner:1.6.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.6.1")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.6.1")
}

tasks.test {
    useJUnitPlatform()
}

sonarqube {
    properties {
        property("sonar.projectName", "mounted-secrets-utils")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.projectKey", "mounted-secrets-utils")
        property("sonar.projectVersion", "${project.version}")
        property("sonar.junit.reportPaths", "${projectDir}/build/test-results/test")
        property("sonar.coverage.jacoco.xmlReportPaths", "${projectDir}/build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", "**/R.java")
    }
}

apply(from = "$rootDir/gradle/includes/codestyle.gradle.kts")
tasks.build {
    dependsOn(arrayOf("checkstyleMain", "checkstyleTest"))
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.destination = file("${buildDir}/jacocoHtml")
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            enabled = true
            limit {
                minimum = "0.2".toBigDecimal()
            }
        }

        rule {
            enabled = false
            element = "BUNDLE"
            includes = listOf("com.github.starter.*")
            excludes = listOf("**/Application*")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.1".toBigDecimal()
            }
        }
    }
}

tasks.test {
    extensions.configure(JacocoTaskExtension::class) {
        destinationFile = file("$buildDir/jacoco/jacocoTest.exec")
        classDumpDir = file("$buildDir/jacoco/classpathdumps")
    }
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

tasks.check {
    dependsOn(arrayOf("jacocoTestReport", "jacocoTestCoverageVerification"))
}


val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

artifacts {
    add("archives", sourcesJar)
}


publishing {
    repositories {
        maven {
            var uploadUrl: String = if (project.extra["release"] == "true") {
                "${project.extra["upload.release.url"]}"
            } else {
                "${project.extra["upload.snapshot.url"]}"
            }
            url = uri(uploadUrl)
            credentials {
                username = "${project.extra["maven.username"]}"
                password = "${project.extra["maven.password"]}"
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = "${project.extra["signing.keyId"]}"
            extra["signing.secretKeyRingFile"] = "${project.extra["signing.secretKeyRingFile"]}"
            extra["signing.password"] = "${project.extra["signing.password"]}"
        }
    }
}

signing {
    sign(configurations.archives.get())
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.extra["release"]=="true"  }
}