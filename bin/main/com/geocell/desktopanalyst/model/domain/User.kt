package com.geocell.desktopanalyst.model.domain

import java.time.LocalDateTime

/**
 * Data class representing a user account in the GeoCell Desktop Analyst system.
 *
 * This domain model encapsulates user authentication, authorization, and profile information
 * for system access control and user management. It follows common user management patterns
 * with support for authentication, permissions, and user activity tracking.
 *
 * The class provides comprehensive user management capabilities including authentication status,
 * permission levels, and temporal tracking of user activity and account lifecycle.
 *
 * @property id the unique database identifier for the user account
 * @property password the encrypted password hash for user authentication
 * @property lastLogin the timestamp of the user's most recent successful login
 * @property isSuperuser indicates if the user has full system administration privileges
 * @property username the unique username for system authentication and identification
 * @property firstName the user's first name for personalization and display
 * @property lastName the user's last name for personalization and display
 * @property email the user's email address for communication and account recovery
 * @property isStaff indicates if the user has access to staff/admin functionality
 * @property isActive indicates if the user account is currently active and enabled
 * @property dateJoined the timestamp when the user account was originally created
 *
 * @sample
 * ```
 * val adminUser = User(
 *     id = 1,
 *     password = "encrypted_password_hash",
 *     lastLogin = LocalDateTime.of(2024, 1, 15, 14, 30, 0),
 *     isSuperuser = true,
 *     username = "admin",
 *     firstName = "System",
 *     lastName = "Administrator",
 *     email = "admin@geocell.com",
 *     isStaff = true,
 *     isActive = true,
 *     dateJoined = LocalDateTime.of(2023, 1, 1, 0, 0, 0)
 * )
 * ```
 *
 * @sample
 * ```
 * val analystUser = User(
 *     id = 2,
 *     password = "encrypted_analyst_password",
 *     lastLogin = LocalDateTime.of(2024, 1, 14, 9, 15, 0),
 *     isSuperuser = false,
 *     username = "analyst.john",
 *     firstName = "John",
 *     lastName = "Doe",
 *     email = "john.doe@geocell.com",
 *     isStaff = true,
 *     isActive = true,
 *     dateJoined = LocalDateTime.of(2023, 6, 15, 10, 0, 0)
 * )
 * ```
 *
 * @since 1.0.0
 */
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