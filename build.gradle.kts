plugins {
    id("java")
}

group = "org.example"
version = "1.0.3-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bzethmayr.fungu:fungu:1.5.7")

    testImplementation("io.github.bzethmayr.fungu:fungu-test:1.2.12")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
}

tasks.test {
    useJUnitPlatform()
}
