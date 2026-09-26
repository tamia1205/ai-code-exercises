# Java Performance Example - Image Processing

## Building and Running

```bash
# Build the project
./gradlew build

# Run the ImageProcessor application
./gradlew run

# Run with specific heap size (512MB)
./gradlew run -Pmax-heap-size=512m

# For Windows users:
# Use gradlew.bat instead:
gradlew.bat build
gradlew.bat run
gradlew.bat run -Pmax-heap-size=512m
```

## Performance Monitoring

### Using JVM Arguments
```bash
# Enable detailed GC logging
./gradlew run -PjvmArgs="-XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

# Enable JFR (Java Flight Recorder)
./gradlew run -PjvmArgs="-XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=recording.jfr"
```

### Using Visual VM
1. Start VisualVM (included in JDK)
2. Run the application
3. Monitor:
    - Heap memory usage
    - GC activity
    - CPU usage
    - Thread states

## Profiling

Use Java profiling tools to identify bottlenecks:

```bash
# Using Java Mission Control
./gradlew run -PjvmArgs="-XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=recording.jfr"

# Using async-profiler
./profiler.sh -d 30 -f profile.html $(pgrep -f ImageProcessor)
```

## Memory Tuning

Adjust JVM parameters based on your requirements:
```bash
./gradlew run -PjvmArgs="-Xmx2g -Xms1g -XX:+UseG1GC"
```

Key parameters:
- `-Xmx`: Maximum heap size
- `-Xms`: Initial heap size
- `-XX:+UseG1GC`: Use G1 Garbage Collector

## Monitoring Tools
- JConsole
- VisualVM
- Java Mission Control
- async-profiler
- JFR (Java Flight Recorder)

