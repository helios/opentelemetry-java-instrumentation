val stableVersion = "1.0.3"
val alphaVersion = "1.0.3-alpha-SNAPSHOT"

allprojects {
  if (findProperty("otel.stable") != "true") {
    version = alphaVersion
  } else {
    version = stableVersion
  }
}
