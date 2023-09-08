plugins {
  id("otel.javaagent-testing")
}

dependencies {
  implementation(project(":testing-common:library-for-integration-tests"))

  testCompileOnly(project(":instrumentation-api"))
  testCompileOnly(project(":javaagent-tooling"))
  testCompileOnly(project(":javaagent-extension-api"))
  testCompileOnly(project(":muzzle"))

  testImplementation("net.bytebuddy:byte-buddy")
  testImplementation("net.bytebuddy:byte-buddy-agent")

  testImplementation("com.google.guava:guava")

  testImplementation("cglib:cglib:3.2.5")

  // test instrumenting java 1.1 bytecode
  // TODO do we want this?
  testImplementation("net.sf.jt400:jt400:6.1")
}

tasks {
  val testFieldInjectionDisabled by registering(Test::class) {
    filter {
      includeTestsMatching("context.FieldInjectionDisabledTest")
    }
    include("**/FieldInjectionDisabledTest.*")
    jvmArgs("-Dotel.javaagent.experimental.field-injection.enabled=false")
  }

  val testFieldBackedImplementation by registering(Test::class) {
    filter {
      includeTestsMatching("context.FieldBackedImplementationTest")
    }
    include("**/FieldBackedImplementationTest.*")
    // this test uses reflection to access fields generated by FieldBackedProvider
    // internal-reflection needs to be disabled because it removes these fields from reflection results.
    jvmArgs("-Dotel.instrumentation.internal-reflection.enabled=false")
  }

  test {
    filter {
      excludeTestsMatching("context.FieldInjectionDisabledTest")
      excludeTestsMatching("context.FieldBackedImplementationTest")
    }
    // this is needed for AgentInstrumentationSpecificationTest
    jvmArgs("-Dotel.javaagent.exclude-classes=config.exclude.packagename.*,config.exclude.SomeClass,config.exclude.SomeClass\$NestedClass")
  }

  check {
    dependsOn(testFieldInjectionDisabled)
    dependsOn(testFieldBackedImplementation)
  }
}
