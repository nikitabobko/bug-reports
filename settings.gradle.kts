dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://maven.java.net/content/groups/public/")
        }
    }
}
rootProject.name = "Todomail"
include(":android-app")
