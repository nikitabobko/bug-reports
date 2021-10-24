// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra["compose_version"] = "1.0.4"
    extra["nav_version"] = "2.3.5"
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    }
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
