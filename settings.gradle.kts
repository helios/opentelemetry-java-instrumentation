pluginManagement {
  plugins {
    id("com.bmuschko.docker-remote-api") version "7.3.0"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.github.jk1.dependency-license-report") version "2.1"
    id("com.google.cloud.tools.jib") version "3.2.1"
    id("com.gradle.plugin-publish") version "1.0.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.unbroken-dome.test-sets") version "4.0.0"
    id("org.xbib.gradle.plugin.jflex") version "1.6.0"
    id("org.unbroken-dome.xjc") version "2.0.0"
  }

  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

plugins {
  id("com.gradle.enterprise") version "3.10.3"
  id("com.github.burrunan.s3-build-cache") version "1.3"
  id("com.gradle.common-custom-user-data-gradle-plugin") version "1.7.2"
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    mavenLocal()
  }
}

val gradleEnterpriseServer = "https://ge.opentelemetry.io"
val isCI = System.getenv("CI") != null
val geAccessKey = System.getenv("GRADLE_ENTERPRISE_ACCESS_KEY") ?: ""

// if GE access key is not given and we are in CI, then we publish to scans.gradle.com
val useScansGradleCom = isCI && geAccessKey.isEmpty()

if (useScansGradleCom) {
  gradleEnterprise {
    buildScan {
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
      isUploadInBackground = !isCI
      publishAlways()

      capture {
        isTaskInputFiles = true
      }
    }
  }
} else {
  gradleEnterprise {
    server = gradleEnterpriseServer
    buildScan {
      isUploadInBackground = !isCI

      this as com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures
      publishIfAuthenticated()
      publishAlways()

      capture {
        isTaskInputFiles = true
      }

      gradle.startParameter.projectProperties["testJavaVersion"]?.let { tag(it) }
      gradle.startParameter.projectProperties["testJavaVM"]?.let { tag(it) }
      gradle.startParameter.projectProperties["smokeTestSuite"]?.let {
        value("Smoke test suite", it)
      }
    }
  }
}

val geCacheUsername = System.getenv("GE_CACHE_USERNAME") ?: ""
val geCachePassword = System.getenv("GE_CACHE_PASSWORD") ?: ""
buildCache {
  remote<HttpBuildCache> {
    url = uri("$gradleEnterpriseServer/cache/")
    isPush = isCI && geCacheUsername.isNotEmpty()
    credentials {
      username = geCacheUsername
      password = geCachePassword
    }
  }
}

rootProject.name = "opentelemetry-java-instrumentation"

includeBuild("conventions")

include(":muzzle")

// agent projects
include(":opentelemetry-api-shaded-for-instrumenting")
include(":opentelemetry-ext-annotations-shaded-for-instrumenting")
include(":opentelemetry-instrumentation-annotations-shaded-for-instrumenting")
include(":opentelemetry-instrumentation-api-shaded-for-instrumenting")
include(":javaagent-bootstrap")
include(":javaagent-extension-api")
include(":javaagent-tooling")
include(":javaagent-tooling:javaagent-tooling-java9")
include(":javaagent")

include(":bom-alpha")
include(":instrumentation-api")
include(":instrumentation-api-semconv")
include(":instrumentation-appender-api-internal")
include(":instrumentation-appender-sdk-internal")
include(":instrumentation-annotations-support")
include(":instrumentation-annotations-support-testing")

// misc
include(":dependencyManagement")
include(":testing:agent-exporter")
include(":testing:agent-for-testing")
include(":testing:armeria-shaded-for-testing")
include(":testing-common")
include(":testing-common:integration-tests")
include(":testing-common:library-for-integration-tests")

// smoke tests
include(":smoke-tests")
include(":smoke-tests:images:fake-backend")
include(":smoke-tests:images:grpc")
include(":smoke-tests:images:play")
include(":smoke-tests:images:quarkus")
include(":smoke-tests:images:servlet")
include(":smoke-tests:images:servlet:servlet-3.0")
include(":smoke-tests:images:servlet:servlet-5.0")
include(":smoke-tests:images:spring-boot")

include(":instrumentation:internal:internal-class-loader:javaagent")
include(":instrumentation:internal:internal-class-loader:javaagent-integration-tests")
include(":instrumentation:internal:internal-eclipse-osgi-3.6:javaagent")
include(":instrumentation:internal:internal-lambda:javaagent")
include(":instrumentation:internal:internal-lambda-java9:javaagent")
include(":instrumentation:internal:internal-reflection:javaagent")
include(":instrumentation:internal:internal-reflection:javaagent-integration-tests")
include(":instrumentation:internal:internal-url-class-loader:javaagent")
include(":instrumentation:internal:internal-url-class-loader:javaagent-integration-tests")
include(":instrumentation:demo:javaagent")
include(":instrumentation:methods:javaax3gent")
include(":instrumentation:opentelemetry-api:opentelemetry-api-1.0:javaagent")
include(":instrumentation:opentelemetry-api:opentelemetry-api-1.4:javaagent")
include(":instrumentation:opentelemetry-api:opentelemetry-api-1.10:javaagent")
include(":instrumentation:opentelemetry-instrumentation-api:javaagent")
include(":instrumentation:opentelemetry-instrumentation-api:testing")

// benchmark
include(":benchmark-overhead-jmh")
include(":benchmark-jfr-analyzer")
