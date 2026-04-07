package com.mystarnow.backend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
class CategoryEntity(
    @Id
    @Column(name = "category_code", nullable = false, length = 64)
    var categoryCode: String,

    @Column(name = "display_name", nullable = false, length = 120)
    var displayName: String,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @Column(name = "is_enabled", nullable = false)
    var enabled: Boolean = true,
) : AuditedEntity()

