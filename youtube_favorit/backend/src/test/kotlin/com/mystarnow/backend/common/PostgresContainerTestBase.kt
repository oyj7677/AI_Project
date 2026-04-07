package com.mystarnow.backend.common

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.nio.file.Files
import java.nio.file.Path

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = [
    "app.operator-auth.enabled=true",
    "app.operator-auth.username=operator",
    "app.operator-auth.password=operator",
    "app.sample-data.enabled=true",
])
@ActiveProfiles("test")
abstract class PostgresContainerTestBase {
    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun truncateTables() {
        jdbcTemplate.execute(
            """
            truncate table
              member_serving_state,
              group_serving_state,
              youtube_raw_source_records,
              youtube_sync_metadata,
              youtube_video_operator_metadata,
              youtube_channel_operator_metadata,
              member_operator_metadata,
              group_operator_metadata,
              youtube_videos,
              youtube_channels,
              idol_members,
              idol_groups,
              platform_sync_metadata,
              influencer_serving_state,
              influencer_operator_metadata,
              channel_operator_metadata,
              raw_source_records,
              live_status_cache,
              activity_items,
              schedule_items,
              channels,
              influencer_categories,
              influencers
            restart identity cascade
            """.trimIndent(),
        )
        jdbcTemplate.execute("delete from categories")
        jdbcTemplate.execute("delete from platforms")
        jdbcTemplate.execute(
            """
            insert into platforms (platform_code, display_name, support_mode, is_enabled, created_at, updated_at) values
            ('youtube','YouTube','auto',true, now(), now()),
            ('instagram','Instagram','limited',true, now(), now()),
            ('x','X','disabled',false, now(), now()),
            ('chzzk','CHZZK','disabled',false, now(), now()),
            ('soop','SOOP','disabled',false, now(), now())
            """.trimIndent(),
        )
        jdbcTemplate.execute(
            """
            insert into categories (category_code, display_name, sort_order, is_enabled, created_at, updated_at) values
            ('game','게임',10,true, now(), now()),
            ('talk','토크',20,true, now(), now()),
            ('daily','일상',30,true, now(), now())
            """.trimIndent(),
        )
    }

    companion object {
        init {
            val colimaSocket = Path.of(System.getProperty("user.home"), ".colima", "default", "docker.sock")
            if (Files.exists(colimaSocket)) {
                System.setProperty("docker.host", "unix://${colimaSocket.toAbsolutePath()}")
                System.setProperty("docker.api.version", "1.53")
                System.setProperty("DOCKER_API_VERSION", "1.53")
                System.setProperty("docker.tls.verify", "0")
            }
        }

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("mystarnow")
            .withUsername("mystarnow")
            .withPassword("mystarnow")

        @JvmStatic
        @DynamicPropertySource
        fun register(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
