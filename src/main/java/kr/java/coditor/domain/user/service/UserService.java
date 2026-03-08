package kr.java.coditor.domain.user.service;

import kr.java.coditor.domain.user.dto.UserResponseDto;
import kr.java.coditor.domain.user.dto.UserUpdateDto;
import kr.java.coditor.domain.user.entity.Provider;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.entity.UserProfile;
import kr.java.coditor.domain.user.repository.UserRepository;
import kr.java.coditor.global.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findByEmail(email);
        return new UserResponseDto(user);
    }

    @Transactional
    public void updateUserInfo(UserUpdateDto updateDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findByEmail(email);

        if (StringUtils.hasText(updateDto.getNickname())) {
            user.updateNickname(updateDto.getNickname());
        }

        if (StringUtils.hasText(updateDto.getPassword())) {
            if (user.getProvider() == Provider.LOCAL) {
                user.updatePassword(passwordEncoder.encode(updateDto.getPassword()));
            } else {
                throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
            }
        }

        UserProfile userProfile = user.getUserProfile();
        if (userProfile == null) {
            userProfile = UserProfile.builder()
                    .user(user)
                    .phoneNumber(updateDto.getPhoneNumber())
                    .introduce(updateDto.getIntroduce())
                    .build();
            user.setUserProfile(userProfile);
        } else {
            userProfile.update(updateDto.getPhoneNumber(), updateDto.getIntroduce());
        }
    }

    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일로 가입된 사용자가 없습니다."));

        if (user.getProvider() != Provider.LOCAL) {
            throw new RuntimeException("소셜 로그인 사용자는 비밀번호를 재설정할 수 없습니다.");
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        user.updatePassword(passwordEncoder.encode(tempPassword));

        String subject = "[Coditor] 임시 비밀번호 발급 안내";
        String text = "안녕하세요, Coditor입니다.\n\n" +
                "요청하신 임시 비밀번호는 다음과 같습니다:\n" +
                tempPassword + "\n\n" +
                "로그인 후 반드시 비밀번호를 변경해주세요.";

        emailService.sendEmail(email, subject, text);
    }
}
