package kr.java.coditor.domain.user.dto;

import kr.java.coditor.domain.user.entity.Provider;
import kr.java.coditor.domain.user.entity.Role;
import kr.java.coditor.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {
    private String email;
    private String password;
    private String nickname;

    public User toUser(PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build();
    }
}
