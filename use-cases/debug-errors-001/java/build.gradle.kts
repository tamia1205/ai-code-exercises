plugins {
    id("java")
    id("application")
}

group = "za.co.wethinkcode"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("za.co.wethinkcode.taskmanager.cli.TaskManagerCli")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-cli:commons-cli:1.9.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // JUnit Jupiter dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito for mocking in tests
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")

    // AssertJ for fluent assertions
    testImplementation("org.assertj:assertj-core:3.24.2")}

tasks.test {
    useJUnitPlatform()

    // Enable test logging
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}