plugins {
    kotlin("jvm") version "1.9.23"          // make sure the Kotlin plugin itself supports Java 21
    id("java")
    id("jacoco")
    id("info.solidsoft.pitest") version "1.15.0"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

group = "net.bzethmayr.mar"
version = "0.4.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"

dependencies {
    implementation("io.github.bzethmayr.fungu:fungu:1.5.7")
    implementation("io.github.rctcwyvrn:blake3:1.3")
    implementation("io.whitfin:siphash:3.0.0")
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-vulkan:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-vma:$lwjglVersion")

    testImplementation("io.github.bzethmayr.fungu:fungu-test:1.2.12")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
        resources.srcDirs("src/main/resources", "src/main/hlsl")
    }
}

val dxcPath = project.rootDir.resolve("tools/dxc/dxc").absolutePath

val hlslDir = file("src/main/hlsl")
val spvOutDir = file("build/generated/spv")

tasks.register("compileHlsl") {
    inputs.dir(hlslDir)
    outputs.dir(spvOutDir)

    doLast {
        spvOutDir.mkdirs()

        hlslDir.walkTopDown()
            .filter { it.isFile && it.extension == "hlsl" }
            .forEach { src ->
                val out = spvOutDir.resolve(src.nameWithoutExtension + ".spv")
                exec {
                    commandLine(
                        dxcPath,
                        "-spirv",
                        "-T", "cs_6_0",        // compute shader target
                        "-E", "main",          // entry point
                        "-Fo", out.absolutePath,
                        src.absolutePath
                    )
                }
            }
    }
}

tasks.processResources {
    dependsOn("compileHlsl")
    // Specify the directory where your tools generate files
    from(layout.buildDirectory.dir("generated/spv")) {
        into("spv") // Optional: place them in a specific subfolder inside the JAR
    }
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
