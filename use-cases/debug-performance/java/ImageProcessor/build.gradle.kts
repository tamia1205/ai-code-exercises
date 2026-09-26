plugins {
    id("java")
    id("application")
}

group = "za.co.wethinkcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.example.images.ImageProcessor")
}

tasks.test {
    useJUnitPlatform()
}

// Configure JVM arguments for the run task
tasks.named<JavaExec>("run") {
    // Allow passing JVM arguments via -PjvmArgs command line parameter
    if (project.hasProperty("jvmArgs")) {
        jvmArgs = (project.property("jvmArgs") as String).split("\\s+").toList()
    }
    
    // Allow setting max heap size
    if (project.hasProperty("max-heap-size")) {
        maxHeapSize = project.property("max-heap-size") as String
    }
}