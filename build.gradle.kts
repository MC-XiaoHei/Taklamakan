@file:Suppress("SpellCheckingInspection")

import org.leavesmc.LeavesPluginJson.Load
import org.leavesmc.leavesPluginJson
import xyz.jpenilla.runtask.service.DownloadsAPIService
import xyz.jpenilla.runtask.service.DownloadsAPIService.Companion.registerIfAbsent

plugins {
    java
    alias(libs.plugins.leavesweightUserdev)
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.resourceFactory)
    kotlin("jvm")
}

group = "cn.xor7.xiaohei"
version = "1.0.0-SNAPSHOT"

val pluginJson = leavesPluginJson {
    main = "cn.xor7.xiaohei.taklamakan.TaklamakanPlugin"
    authors.add("MC_XiaoHei")
    description = "神秘赛博徒步"
    foliaSupported = false
    apiVersion = libs.versions.leavesApi.extractMCVersion()
    dependencies.server("voicechat", required = true, load = Load.BEFORE, joinClasspath = true)
}

val runServerPlugins = runPaper.downloadPluginsSpec {
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.leavesmc.org/releases/") {
        name = "leavesmc-releases"
    }
    maven("https://repo.leavesmc.org/snapshots/") {
        name = "leavesmc-snapshots"
    }
    maven("https://maven.maxhenkel.de/repository/public") {
        name = "svc-repo"
    }
    mavenLocal()
}

sourceSets {
    main {
        resourceFactory {
            factories(pluginJson.resourceFactory())
        }
    }
}

dependencies {
    apply `plugin dependencies`@{
        implementation(kotlin("stdlib-jdk8"))
        compileOnly("de.maxhenkel.voicechat:voicechat-api:2.6.0")
    }

    apply `api and server source`@{
        compileOnly(libs.leavesApi)
        paperweight.devBundle(libs.leavesDevBundle)
    }
}

tasks {
    runServer {
        downloadsApiService.set(leavesDownloadApiService())
        downloadPlugins.from(runServerPlugins)
        minecraftVersion(libs.versions.leavesApi.extractMCVersion())
        systemProperty("file.encoding", Charsets.UTF_8.name())
    }

    withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.forkOptions.memoryMaximumSize = "6g"

        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }

    shadowJar {
        archiveFileName = "${project.name}-${version}.jar"
    }

    build {
        dependsOn(shadowJar)
    }
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

fun Provider<String>.extractMCVersion(): String {
    val versionString = this.get()
    val regex = Regex("""^(1\.\d+(?:\.\d+)?)""")
    return regex.find(versionString)?.groupValues?.get(1)
        ?: throw IllegalArgumentException("Cannot extract mcVersion from $versionString")
}

fun leavesDownloadApiService(): Provider<out DownloadsAPIService> = registerIfAbsent(project) {
    downloadsEndpoint = "https://api.leavesmc.org/v2/"
    downloadProjectName = "leaves"
    buildServiceName = "leaves-download-service"
}