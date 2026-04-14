plugins {
    kotlin("jvm") version "1.9.23"          // make sure the Kotlin plugin itself supports Java 21
    id("java")
    id("jacoco")
    id("info.solidsoft.pitest") version "1.15.0"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

group = "org.example"
version = "1.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bzethmayr.fungu:fungu:1.5.7")
    implementation("at.yawk.lz4:lz4-java:1.10.2")
    implementation("io.whitfin:siphash:3.0.0")

    testImplementation("io.github.bzethmayr.fungu:fungu-test:1.2.12")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

tasks.test {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

pitest {
    pitestVersion.set("1.15.0")
    junit5PluginVersion.set("1.2.3")
    targetClasses.set(listOf("net.bzethmayr.gigantspinosaurus.*"))
    targetTests.set(listOf("net.bzethmayr.gigantspinosaurus.*Test"))
    outputFormats.set(listOf("HTML", "XML"))
    reportDir.set(file("build/reports/pitest"))
}
