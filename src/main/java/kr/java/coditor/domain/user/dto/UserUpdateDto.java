package kr.java.coditor.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDto {
    private String nickname;
    private String phoneNumber;
    private String introduce;
    private String password;
}
