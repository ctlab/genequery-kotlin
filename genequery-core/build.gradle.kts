plugins {
    kotlin("jvm")
}


dependencies {
    implementation(kotlin("stdlib"))
    testCompile(group="org.jetbrains.kotlin", name="kotlin-test", version="1.1.51")
    testCompile("junit:junit:4.12")
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}