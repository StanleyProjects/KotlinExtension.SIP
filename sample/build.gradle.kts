repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}


plugins {
    id("application")
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":lib"))
    implementation("com.github.kepocnhh:KotlinExtension.Functional:0.3-SNAPSHOT")
}

application {
    mainClass.set("sp.service.sample.AppKt")
}

val jvmTarget = "1.8"

tasks.getByName<JavaCompile>("compileJava") {
    targetCompatibility = jvmTarget
}

tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    kotlinOptions.jvmTarget = jvmTarget
}
