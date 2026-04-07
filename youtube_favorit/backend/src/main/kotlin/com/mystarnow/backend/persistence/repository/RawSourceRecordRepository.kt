package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.RawSourceRecordEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RawSourceRecordRepository : JpaRepository<RawSourceRecordEntity, UUID>

