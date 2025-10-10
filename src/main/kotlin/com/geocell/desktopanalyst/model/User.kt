package com.geocell.desktopanalyst.model

import java.time.LocalDateTime

data class User(
    val id: Long,
    val password: String,
    val lastLogin: LocalDateTime?,
    val isSuperuser: Boolean,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val isStaff: Boolean,
    val isActive: Boolean,
    val dateJoined: LocalDateTime,
)
