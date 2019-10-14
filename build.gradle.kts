plugins {
    base
    kotlin("jvm") version "1.3.50" apply false
}

allprojects {
    group = "ru.ifmo.genequery"
    version = "1.1-SNAPSHOT"

    repositories {
        jcenter()
    }

}

tasks.register<Copy>("copyJarToRoot") {
    from("genequery-rest/build/lib/genequery-rest-${version}.jar")
    into("./gq-rest.jar")
}
