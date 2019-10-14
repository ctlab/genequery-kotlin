plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "2.1.9.RELEASE"
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(kotlin("stdlib"))

    compile(project(":genequery-core"))

    compile(group="log4j", name="log4j", version="1.2.17")
    compile("org.springframework.boot:spring-boot-starter-web:2.1.9.RELEASE")
    compile("org.springframework.boot:spring-boot-configuration-processor:2.1.9.RELEASE")
    compile("org.springframework.boot:spring-boot-starter-jetty:2.1.9.RELEASE")

    testCompile(group="org.jetbrains.kotlin", name="kotlin-test", version="1.1.51")
    testCompile("junit:junit:4.12")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("com.jayway.jsonpath:json-path:2.4.0")

}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

springBoot {
    mainClassName = "gq.rest.Application"
}
