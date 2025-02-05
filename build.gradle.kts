plugins {
    application
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta6"
}

group = "me.kumo"
version = "0.0.1"
application {
    mainClass = "me.kumo.drone.Main"
}
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven {
        url = uri("https://www.jogamp.org/deployment/maven/")
        name = "jogamp-remote"
    }
}

val jmeVersion = "3.7.0-stable"
dependencies {
    implementation("org.apache.groovy:groovy-all:4.0.24")
    implementation("org.jmonkeyengine:jme3-core:$jmeVersion")
    implementation("org.jmonkeyengine:jme3-desktop:$jmeVersion")
    implementation("org.jmonkeyengine:jme3-lwjgl3:$jmeVersion")
    implementation("org.jmonkeyengine:jme3-terrain:$jmeVersion")
    implementation("org.jmonkeyengine:jme3-plugins:$jmeVersion")
    implementation("org.jmonkeyengine:jme3-testdata:$jmeVersion")
//    implementation("org.jmonkeyengine:jme3-awt-dialogs:$jmeVersion")

    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.simsilica:lemur:1.16.0")

    implementation("com.github.stephengold:SkyControl:1.1.0")

    implementation("com.fazecast:jSerialComm:2.11.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("res")
    }
    test {
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
        java {
            java.srcDirs("test")
        }
    }
}
tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest {
        attributes["Implementation-Version"] = project.version
    }
}