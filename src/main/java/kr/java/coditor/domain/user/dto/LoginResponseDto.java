package kr.java.coditor.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String nickname;
    private String role;
	private Long memberId;
}
