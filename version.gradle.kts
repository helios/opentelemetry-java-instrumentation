val stableVersion = "1.0.6"
val alphaVersion = "1.17.1-alpha-SNAPSHOT"

allprojects {
  if (findProperty("otel.stable") != "true") {
    version = alphaVersion
  } else {
    version = stableVersion
  }
}
