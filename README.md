[Upstream project README](https://github.com/open-telemetry/opentelemetry-java/blob/main/README.md)

## Build Locally

1. Run:

   ```
     ./gradlew build
     -x :instrumentation:kafka:kafka-streams-0.11:javaagent:testReceiveSpansDisabled
     -x :instrumentation:spring:spring-webmvc-3.1:javaagent:codenarcTest
     -x :instrumentation:vertx:vertx-kafka-client-3.6:javaagent:testNoReceiveTelemetry
     -x test
   ```

2. Pray 🙏
3. The updated agent jar should be under `javaagent/build/libs`.

## Release

1. Bump the stable version in the following file: `version.gradle.kts`.
2. Open a PR (pray some more 🙏) and merge to main.
3. Trigger [Release](https://github.com/helios/opentelemetry-java-instrumentation/actions/workflows/release.yml) workflow in github.
