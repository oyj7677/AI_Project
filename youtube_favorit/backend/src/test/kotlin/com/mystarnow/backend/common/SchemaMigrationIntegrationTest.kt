package com.mystarnow.backend.common

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaMigrationIntegrationTest : PostgresContainerTestBase() {
    @Test
    fun `flyway creates hardening tables`() {
        val tables = jdbcTemplate.queryForList(
            """
            select table_name
            from information_schema.tables
            where table_schema = 'public'
            """.trimIndent(),
            String::class.java,
        ).toSet()

        assertTrue("platform_sync_metadata" in tables)
        assertTrue("influencer_serving_state" in tables)
        assertTrue("raw_source_records" in tables)
        assertTrue("influencer_operator_metadata" in tables)
        assertTrue("channel_operator_metadata" in tables)
    }
}

