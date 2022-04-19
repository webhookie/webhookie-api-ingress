import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot") version "2.6.6"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version "1.6.10"
  kotlin("plugin.spring") version "1.6.10"
  kotlin("plugin.allopen") version "1.6.10"
  kotlin("kapt") version "1.6.10"
}

kapt.includeCompileClasspath = false

allOpen {
  annotation("com.webhookie.common.annotation.Open")
  // annotations("com.another.Annotation", "com.third.Annotation")
}

group = "com.webhookie"
version = "2.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
}

extra["springCloudVersion"] = "2021.0.1"

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

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.named<BootJar>("bootJar") {
  layered {
    application {
      intoLayer("spring-boot-loader") {
        include("org/springframework/boot/loader/**")
      }
      intoLayer("application")
    }
    dependencies {
      intoLayer("snapshot-dependencies") {
        include("*:*:*SNAPSHOT")
      }
      intoLayer("internal-dependencies") {
        include("com.webhookie:*:*")
      }
      intoLayer("dependencies")
    }
    layerOrder = listOf("dependencies", "spring-boot-loader", "internal-dependencies", "snapshot-dependencies", "application")
  }
}

tasks.named<BootBuildImage>("bootBuildImage") {
  imageName = "webhookie.com/services/${project.name.replace("webhookie-", "")}"
}

springBoot {
  buildInfo()
}
