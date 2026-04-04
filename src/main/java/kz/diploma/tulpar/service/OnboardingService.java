package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.enums.DailyGoal;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.exception.ResourceNotFoundException;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "user-profile", key = "#uid")
    public void setLevel(String uid, DifficultyLevel level) {
        var user = userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));
        user.setLevel(level);
    }

    @Transactional
    @CacheEvict(value = "user-profile", key = "#uid")
    public void setDailyGoal(String uid, DailyGoal dailyGoal) {
        var user = userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));
        user.setDailyGoal(dailyGoal);
    }

    @Transactional
    @CacheEvict(value = "user-profile", key = "#uid")
    public void complete(String uid) {
        var user = userRepository.findById(uid)
                .orElseThrow(() -> ResourceNotFoundException.of("User", uid));
        user.setOnboardingCompleted(true);
    }
}
