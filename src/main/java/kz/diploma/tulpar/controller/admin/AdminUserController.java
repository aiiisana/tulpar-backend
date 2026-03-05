package kz.diploma.tulpar.controller.admin;

import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.enums.UserRole;
import kz.diploma.tulpar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADMIN-only user management endpoints.
 * CONTENT_MANAGER cannot access these — restricted further via @PreAuthorize.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * GET /admin/users?role=USER
     * List users by role. ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> listUsers(
            @RequestParam(defaultValue = "USER") UserRole role) {
        return ResponseEntity.ok(userService.findAllByRole(role));
    }

    /**
     * PATCH /admin/users/{uid}/role?newRole=CONTENT_MANAGER
     * Promote or demote a user. ADMIN only.
     */
    @PatchMapping("/{uid}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRole(
            @PathVariable String uid,
            @RequestParam UserRole newRole) {
        return ResponseEntity.ok(userService.updateRole(uid, newRole));
    }
}
