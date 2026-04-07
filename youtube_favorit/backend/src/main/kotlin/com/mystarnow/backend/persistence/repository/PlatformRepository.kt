package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.PlatformEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PlatformRepository : JpaRepository<PlatformEntity, String>

