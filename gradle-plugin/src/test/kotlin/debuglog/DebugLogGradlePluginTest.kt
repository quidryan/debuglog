package debuglog

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner


import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.containsString

import org.junit.Assert.assertThat
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runners.MethodSorters

import java.io.File

@FixMethodOrder(MethodSorters.JVM)
class DebugLogGradlePluginTest {
    @Test
    fun `debuglog can be configured`() {

        givenBuildScript("""
            plugins {
                id("debuglog.plugin")
            }
            debugLog {
                enabled = true
                annotations = listOf("java.lang.Deprecated")
            }
        """)

        assertThat(
                build("model", "-q").output.trimEnd(),
                containsString("Root project")) // No exception thrown
    }

    @Test
    fun `debuglog errors if not configured`() {

        givenBuildScript("""
            plugins {
                id("debuglog.plugin")
                kotlin("jvm")  version "1.3.30"
            }
            repositories { jcenter() }
            dependencies {
                implementation(kotlin("stdlib"))
            }
            debugLog {
                enabled = false
                annotations = listOf("java.lang.Deprecated")
            }
        """)

        givenSource("""
            package debuglogtest

            import java.lang.Deprecated

            class Main {
                @Deprecated fun addOne(n: Int): Int = n + 1
            }

            fun main(args : Array<String>) {
                println(Main().addOne(3))
            }

        """.trimIndent())

        val message = "DebugLog is enabled, but no annotations were set"
        assertThat(
                build("assemble", "-q", "--stacktrace", "--debug").output.trimEnd(),
                equalTo(message))
    }

    @Test
    fun `debuglog runs`() {

        givenBuildScript("""
            plugins {
                id("debuglog.plugin")
                id("application")
                kotlin("jvm")  version "1.3.30"
            }
            repositories { jcenter() }
            dependencies {
                implementation(kotlin("stdlib"))
            }
            application {
                mainClassName = "debuglogtest.MainKt"
            }
            debugLog {
                enabled = true
                annotations = listOf("java.lang.Deprecated")
            }
        """)

        givenSource("""
            package debuglogtest

            import java.lang.Deprecated

            class Main {
                @Deprecated fun addOne(n: Int): Int = n + 1
            }

            fun main(args : Array<String>) {
                println(Main().addOne(3))
            }

        """.trimIndent())
        assertThat(
                build("run", "-q", "--stacktrace").output.trimEnd(),
                equalTo("4"))
    }

    private
    fun build(vararg arguments: String): BuildResult =
            GradleRunner
                    .create()
                    .withProjectDir(temporaryFolder.root)
                    .withPluginClasspath()
                    .withArguments(*arguments)
            // .withDebug(true) // Will fail with "Type org.gradle.api.tasks.SourceSet not present"
                    .build()

    private
    fun givenBuildScript(script: String) =
            newFile("build.gradle.kts").apply {
                writeText(script)
            }

    private
    fun givenSource(script: String) =
            newFile("src/main/kotlin/debuglogtest/Main.kt").apply {
                writeText(script)
            }


    private
    fun newFile(fileName: String): File {
        val file = File(temporaryFolder.root, fileName)
        file.parentFile.mkdirs()
        return temporaryFolder.newFile(fileName)
    }

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()
}