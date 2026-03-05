package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.enums.UserRole;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findById(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));
    }

    /** Admin only — list all users with a given role. */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<User> findAllByRole(UserRole role) {
        return userRepository.findAllByRole(role);
    }

    /** Admin only — promote or demote a user's role. */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public User updateRole(String uid, UserRole newRole) {
        User user = findById(uid);
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
