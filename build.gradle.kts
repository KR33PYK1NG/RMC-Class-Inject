// ---------- //

val rmcRepoUser: String by project
val rmcRepoPass: String by project

val rmcGroup = "rmc.libs"
val rmcArtifact = "class-inject"
val rmcVersion = "1.0.0"
val rmcBaseName = "RMC-Class-Inject"

setGroup(rmcGroup)
setVersion(rmcVersion)

// ---------- //

apply(plugin = "maven-publish")
apply(plugin = "net.minecraftforge.gradle")

tasks.withType<Jar> {
    setProperty("archiveBaseName", rmcBaseName)
    finalizedBy("reobfJar")
}

tasks.withType<JavaCompile> {
    outputs.upToDateWhen { false }
    setSourceCompatibility("1.8")
    setTargetCompatibility("1.8")
}

configure<PublishingExtension> {
    repositories {
        maven {
            setUrl("https://repo.rus-minecraft.ru/repository/maven")
            credentials {
                setUsername(rmcRepoUser)
                setPassword(rmcRepoPass)
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            setArtifactId(rmcArtifact)
            from(components["java"])
        }
    }
}

configure<net.minecraftforge.gradle.userdev.UserDevExtension> {
    mappings("official", "1.16.5")
}

// ---------- //

buildscript {
    repositories {
        maven {
            setUrl("https://maven.minecraftforge.net")
        }
    }
    dependencies {
        add("classpath", "net.minecraftforge.gradle:ForgeGradle:4.1.+")
    }
}

// ---------- //

dependencies {
    add("minecraft", "net.minecraftforge:forge:1.16.5-36.1.32")
}

// ---------- //