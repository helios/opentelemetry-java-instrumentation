plugins {
  id("otel.javaagent-instrumentation")
}

dependencies {
  api(project(":instrumentation:servlet:servlet-common:javaagent"))
  compileOnly(project(":instrumentation:servlet:servlet-common:bootstrap"))
  implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")

  compileOnly("org.apache.tomcat.embed:tomcat-embed-core:7.0.4")
}
