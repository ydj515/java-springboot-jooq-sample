import org.flywaydb.core.Flyway
import org.gradle.api.tasks.testing.Test
import java.sql.DriverManager

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.mysql:mysql-connector-j:9.4.0")
        classpath("org.flywaydb:flyway-core:12.0.1")
        classpath("org.flywaydb:flyway-mysql:12.0.1")
    }
}

plugins {
    java
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jooq.jooq-codegen-gradle") version "3.19.32"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

val localDbHost = providers.environmentVariable("DB_HOST").orElse("localhost").get()
val localDbPort = providers.environmentVariable("DB_PORT").orElse("3306").get()
val localDbName = providers.environmentVariable("DB_DATABASE").orElse("mydatabase").get()
val localDbUser = providers.environmentVariable("DB_USER").orElse("myuser").get()
val localDbPassword = providers.environmentVariable("DB_PASSWORD").orElse("mypassword").get()
val localJdbcUrl =
    "jdbc:mysql://$localDbHost:$localDbPort/$localDbName?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
val codegenDbHost = providers.environmentVariable("JOOQ_CODEGEN_DB_HOST").orElse(localDbHost).get()
val codegenDbPort = providers.environmentVariable("JOOQ_CODEGEN_DB_PORT").orElse(localDbPort).get()
val codegenDbName = providers.environmentVariable("JOOQ_CODEGEN_DB_DATABASE").orElse("jooq_codegen").get()
val codegenDbUser = providers.environmentVariable("JOOQ_CODEGEN_DB_USER").orElse("root").get()
val codegenDbPassword = providers.environmentVariable("JOOQ_CODEGEN_DB_PASSWORD").orElse("root").get()
val codegenAdminJdbcUrl =
    "jdbc:mysql://$codegenDbHost:$codegenDbPort/mysql?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
val codegenJdbcUrl =
    "jdbc:mysql://$codegenDbHost:$codegenDbPort/$codegenDbName?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    compileOnly("jakarta.xml.bind:jakarta.xml.bind-api")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")
    jooqCodegen("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.testcontainers:jdbc")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration-test", "jooq-repository")
    }
}

tasks.withType<Test>().configureEach {
    jvmArgs("-Xshare:off")
}

val webLayerTest by tasks.registering(Test::class) {
    description = "웹 레이어 슬라이스 테스트를 실행합니다."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
    useJUnitPlatform {
        includeTags("web-layer")
    }
}

val jooqRepositoryTest by tasks.registering(Test::class) {
    description = "jOOQ 저장소 테스트를 실행합니다."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
    systemProperty("spring.profiles.active", "test")
    useJUnitPlatform {
        includeTags("jooq-repository")
    }
}

val integrationTest by tasks.registering(Test::class) {
    description = "MySQL Testcontainers 기반 통합 테스트를 실행합니다."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
    systemProperty("spring.profiles.active", "integration-test")
    useJUnitPlatform {
        includeTags("integration-test")
    }
}

val prepareCodegenDatabase by tasks.registering {
    description = "jOOQ codegen용 MySQL 스키마를 clean/migrate 합니다."
    group = "jooq"

    doLast {
        Class.forName("com.mysql.cj.jdbc.Driver")
        var lastException: Exception? = null
        var connected = false

        for (attempt in 0 until 20) {
            try {
                DriverManager.getConnection(codegenAdminJdbcUrl, codegenDbUser, codegenDbPassword).use { connection ->
                    connection.createStatement().use { statement ->
                        statement.execute(
                            """
                            CREATE DATABASE IF NOT EXISTS `$codegenDbName`
                            CHARACTER SET utf8mb4
                            COLLATE utf8mb4_0900_ai_ci
                            """.trimIndent()
                        )
                    }
                }
                connected = true
                break
            } catch (e: Exception) {
                lastException = e
                if (attempt == 19) {
                    throw e
                }
                Thread.sleep(1_000)
            }
        }
        if (!connected && lastException != null) {
            throw lastException
        }

        val flyway = Flyway.configure()
            .dataSource(codegenJdbcUrl, codegenDbUser, codegenDbPassword)
            .locations("filesystem:src/main/resources/db/migration")
            .cleanDisabled(false)
            .connectRetries(10)
            .load()

        flyway.clean()
        flyway.migrate()
    }
}

jooq {
    configuration {
        jdbc {
            driver = "com.mysql.cj.jdbc.Driver"
            url = codegenJdbcUrl
            user = codegenDbUser
            password = codegenDbPassword
        }
        generator {
            database {
                name = "org.jooq.meta.mysql.MySQLDatabase"
                includes = "users|roles|users_roles|customers|purchase_orders|purchase_order_items|payments|payment_histories|outbox_events|processed_events|compensation_tasks|cancellations"
                excludes = "flyway_schema_history"
                outputCatalogToDefault = true
                forcedTypes {
                    forcedType {
                        userType = "org.example.javaspringbootjooqsample.domain.user.UserType"
                        isEnumConverter = true
                        includeExpression = ".*\\.users\\.user_type"
                    }
                    forcedType {
                        userType = "org.example.javaspringbootjooqsample.domain.order.OrderStatus"
                        isEnumConverter = true
                        includeExpression = ".*\\.purchase_orders\\.order_status"
                    }
                    forcedType {
                        userType = "org.example.javaspringbootjooqsample.domain.payment.PaymentStatus"
                        isEnumConverter = true
                        includeExpression = ".*\\.payments\\.status|.*\\.payment_histories\\.(from_status|to_status)"
                    }
                    forcedType {
                        userType = "org.example.javaspringbootjooqsample.domain.outbox.OutboxStatus"
                        isEnumConverter = true
                        includeExpression = ".*\\.outbox_events\\.status"
                    }
                    forcedType {
                        userType = "org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskStatus"
                        isEnumConverter = true
                        includeExpression = ".*\\.compensation_tasks\\.status"
                    }
                    forcedType {
                        userType = "org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskType"
                        isEnumConverter = true
                        includeExpression = ".*\\.compensation_tasks\\.task_type"
                    }
                    forcedType {
                        userType = "org.example.javaspringbootjooqsample.domain.order.CancellationStatus"
                        isEnumConverter = true
                        includeExpression = ".*\\.cancellations\\.status"
                    }
                }
            }
            generate {
                records = true
                daos = false
                pojos = false
                deprecated = false
            }
            target {
                packageName = "org.example.javaspringbootjooqsample.generated"
                directory = "build/generated-src/jooq/main"
                clean = true
            }
        }
    }
}

sourceSets {
    main {
        java.srcDir("build/generated-src/jooq/main")
    }
}

tasks.named("jooqCodegen") {
    dependsOn(prepareCodegenDatabase)
}

tasks.named("compileJava") {
    dependsOn(tasks.named("jooqCodegen"))
}
