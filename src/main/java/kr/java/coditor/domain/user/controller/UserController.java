package kr.java.coditor.domain.user.controller;

import kr.java.coditor.domain.user.dto.ActivityDto;
import kr.java.coditor.domain.user.dto.SolvedProblemDto;
import kr.java.coditor.domain.user.dto.UserResponseDto;
import kr.java.coditor.domain.user.dto.UserUpdateDto;
import kr.java.coditor.domain.user.service.UserPageService;
import kr.java.coditor.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserPageService userPageService;

    @GetMapping("/info")
    public ResponseEntity<UserResponseDto> getMyInfo() {
        UserResponseDto userInfo = userService.getUserInfo();
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/info")
    public ResponseEntity<Void> updateMyInfo(@RequestBody UserUpdateDto updateDto) {
        userService.updateUserInfo(updateDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityDto>> getUserActivity() {
        return ResponseEntity.ok(userPageService.getUserActivity());
    }

    @GetMapping("/solved-problems")
    public ResponseEntity<List<SolvedProblemDto>> getSolvedProblems() {
        return ResponseEntity.ok(userPageService.getSolvedProblems());
    }
}
