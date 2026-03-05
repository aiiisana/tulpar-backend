package kz.diploma.tulpar.domain.enums;

/**
 * System roles for authorization.
 * ADMIN        — full system access
 * CONTENT_MANAGER — can upload media and manage exercises, cannot manage users
 * USER         — learner; read-only access to exercises and own progress
 */
public enum UserRole {
    USER,
    CONTENT_MANAGER,
    ADMIN
}
