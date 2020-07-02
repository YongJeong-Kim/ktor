import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
  application
  kotlin("jvm") version "1.3.70"
}

group = "com.kyj"
version = "0.0.1"

application {
  mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
  mavenLocal()
  jcenter()
  maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {
  implementation("com.zaxxer:HikariCP:3.4.5")
  implementation("com.drewnoakes:metadata-extractor:2.14.0")
  implementation("org.jetbrains.exposed:exposed-core:0.24.1")
  implementation("org.jetbrains.exposed:exposed-jdbc:0.24.1")
  implementation("org.jetbrains.exposed:exposed-jodatime:0.24.1")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-jackson:$ktor_version")
  implementation("mysql:mysql-connector-java:8.0.20")
  testImplementation("io.ktor:ktor-server-tests:$ktor_version")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.7.0-M1")
  testCompile("org.junit.jupiter:junit-jupiter-engine:5.7.0-M1")
  testCompile("org.assertj:assertj-core:3.16.1")
  testCompile("io.rest-assured:rest-assured:4.3.0")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")
