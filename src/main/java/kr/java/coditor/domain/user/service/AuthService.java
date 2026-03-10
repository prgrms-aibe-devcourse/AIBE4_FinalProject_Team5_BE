package kr.java.coditor.domain.user.service;

import kr.java.coditor.domain.user.dto.LoginRequestDto;
import kr.java.coditor.domain.user.dto.LoginResponseDto;
import kr.java.coditor.domain.user.dto.SignUpRequestDto;
import kr.java.coditor.domain.user.dto.TokenResponseDto;
import kr.java.coditor.domain.user.entity.RefreshToken;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.jwt.JwtTokenProvider;
import kr.java.coditor.domain.user.repository.RefreshTokenRepository;
import kr.java.coditor.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signup(SignUpRequestDto signUpRequestDto) {
        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다");
        }

        User user = signUpRequestDto.toUser(passwordEncoder);
        userRepository.save(user);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenResponseDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(token -> {
                    token.updateToken(tokenDto.getRefreshToken());
                    return token;
                })
                .orElse(RefreshToken.builder()
                        .user(user)
                        .token(tokenDto.getRefreshToken())
                        .build());

        refreshTokenRepository.save(refreshToken);

        return LoginResponseDto.builder()
                .accessToken(tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public TokenResponseDto reissue(TokenResponseDto tokenRequestDto) {
        if (!jwtTokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        if (!refreshToken.getToken().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        TokenResponseDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        refreshToken.updateToken(tokenDto.getRefreshToken());

        return tokenDto;
    }

    @Transactional
    public void logout(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token 입니다.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUserId(user.getId());
    }
}
