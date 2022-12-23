plugins {
  kotlin("jvm") version "1.7.22"
  kotlin("plugin.spring") version "1.7.22"
  kotlin("plugin.allopen") version "1.7.22"
  kotlin("kapt") version "1.7.22"
}

kapt.includeCompileClasspath = false

dependencies {
  implementation(project(":webhookie-spring-boot-starter-service"))
  implementation(project(":webhookie-amqp-autoconfigure"))

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.amqp:spring-rabbit-test")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}
