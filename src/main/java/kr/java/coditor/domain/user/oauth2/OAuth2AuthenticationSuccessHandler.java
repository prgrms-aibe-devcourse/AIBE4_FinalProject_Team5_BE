package kr.java.coditor.domain.user.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.java.coditor.domain.user.dto.TokenResponseDto;
import kr.java.coditor.domain.user.entity.RefreshToken;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.jwt.JwtTokenProvider;
import kr.java.coditor.domain.user.repository.RefreshTokenRepository;
import kr.java.coditor.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");

        if (email == null) {
             throw new RuntimeException("이메일 정보를 찾을 수 없습니다.");
        }

        Authentication newAuth = new UsernamePasswordAuthenticationToken(email, null, authentication.getAuthorities());

        TokenResponseDto tokenResponseDto = jwtTokenProvider.generateTokenDto(newAuth);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(token -> {
                    token.updateToken(tokenResponseDto.getRefreshToken());
                    return token;
                })
                .orElse(RefreshToken.builder()
                        .user(user)
                        .token(tokenResponseDto.getRefreshToken())
                        .build());

        refreshTokenRepository.save(refreshToken);

        // 메인 페이지(http://localhost:5173/)로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/")
                .queryParam("accessToken", tokenResponseDto.getAccessToken())
                .queryParam("refreshToken", tokenResponseDto.getRefreshToken())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
