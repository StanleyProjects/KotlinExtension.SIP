buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.android.tools.build:gradle:7.1.3")
    }
}

task<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}
