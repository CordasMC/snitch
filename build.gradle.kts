import org.jooq.meta.jaxb.Property

plugins {
    `java-library`
    alias(libs.plugins.run.paper)
    alias(libs.plugins.jooq.codegen)
}

group = "dev.jstus"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()

    // PaperMC
    maven("https://repo.papermc.io/repository/maven-public/")

    // InvUI
    maven("https://repo.xenondevs.xyz/releases")

    // LiteBans API
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.paper.api)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.invui)

    // Cloud v2 command framework — provided at runtime by the plugin loader
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.paper)
    compileOnly(libs.cloud.minecraft.extras)

    // Persistence
    compileOnly(libs.jooq)
    compileOnly(libs.postgresql)
    compileOnly(libs.flyway.core)
    compileOnly(libs.flyway.postgresql)
    compileOnly(libs.hikari)

    // Config & dependency injection
    compileOnly(libs.configlib.paper)
    compileOnly(libs.guice)
    compileOnly(libs.caffeine)

    // Provided by the LiteBans plugin
    compileOnly(libs.litebans.api)

    // Code generation reads the Flyway migrations, no live database required
    jooqCodegen(libs.jooq.meta.extensions)
    jooqCodegen(libs.postgresql)
}

jooq {
    configuration {
        generator {
            database {
                // Generate from the Flyway migration scripts instead of a live connection.
                // Swap for org.jooq.meta.postgres.PostgresDatabase + a jdbc {} block to read a real database.
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"

                properties.addAll(
                    listOf(
                        // Merged by the jooqDdl task from common/ + h2/ (the canonical dialect for codegen);
                        // the other dialect directories must stay in sync with h2/.
                        Property().withKey("scripts").withValue("build/jooq-ddl/*.sql"),
                        Property().withKey("sort").withValue("flyway"),
                        Property().withKey("defaultNameCase").withValue("lower"),
                        Property().withKey("unqualifiedSchema").withValue("public"),
                    )
                )
            }

            generate {
                isJavaTimeTypes = true
                isFluentSetters = true
            }

            target {
                packageName = "dev.jstus.snitch.storage.jooq"
                directory = layout.buildDirectory.dir("generated-src/jooq").get().asFile.path
            }
        }
    }
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated-src/jooq"))
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }

    withType<Javadoc>() {
        options.encoding = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("26.1.2")
    }

    val jooqDdl by registering(Sync::class) {
        from("src/main/resources/db/migration/common", "src/main/resources/db/migration/h2")
        into(layout.buildDirectory.dir("jooq-ddl"))
    }

    jooqCodegen {
        dependsOn(jooqDdl)
        inputs.dir(layout.buildDirectory.dir("jooq-ddl"))
    }

    compileJava {
        dependsOn(jooqCodegen)
    }

    jar {
        archiveFileName = "${rootProject.name}-${project.version}.jar"

        manifest {
            attributes["Implementation-Version"] = rootProject.version
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    defaultTasks("build")
}
