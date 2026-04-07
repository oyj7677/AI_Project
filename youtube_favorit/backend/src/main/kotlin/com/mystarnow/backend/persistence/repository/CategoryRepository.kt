package com.mystarnow.backend.persistence.repository

import com.mystarnow.backend.persistence.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<CategoryEntity, String> {
    fun findAllByEnabledTrueOrderBySortOrderAsc(): List<CategoryEntity>
}

