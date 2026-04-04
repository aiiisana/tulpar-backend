package kz.diploma.tulpar.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin — Users", description = "User management operations (ADMIN only)")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(
        summary = "List users by role",
        description = "Returns all users that have the specified role. Defaults to USER if no role is provided."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User list returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> listUsers(
            @Parameter(description = "Role to filter by", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserRole.class))
            @RequestParam(defaultValue = "USER") UserRole role) {
        return ResponseEntity.ok(userService.findAllByRole(role));
    }

    @Operation(
        summary = "Change user role",
        description = "Promotes or demotes a user to the specified role. Cannot set ADMIN role via this endpoint (use direct DB access)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role updated, updated user returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid Firebase token", content = @Content),
        @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PatchMapping("/{uid}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRole(
            @Parameter(description = "Firebase UID of the target user", required = true)
            @PathVariable String uid,
            @Parameter(description = "New role to assign", required = true, schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserRole.class))
            @RequestParam UserRole newRole) {
        return ResponseEntity.ok(userService.updateRole(uid, newRole));
    }
}
