val stableVersion = "1.0.5"
val alphaVersion = "1.0.4-alpha-SNAPSHOT"

allprojects {
  if (findProperty("otel.stable") != "true") {
    version = alphaVersion
  } else {
    version = stableVersion
  }
}
