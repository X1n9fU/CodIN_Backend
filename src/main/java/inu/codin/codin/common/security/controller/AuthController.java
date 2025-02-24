package inu.codin.codin.common.security.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.common.security.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.common.security.service.AuthService;
import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.domain.user.dto.request.UserProfileRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping(value = "/auth")
@Tag(name = "User Auth API", description = "유저 회원가입, 로그인, 로그아웃, 리이슈 API")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/google")
    public ResponseEntity<SingleResponse<?>> googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/oauth2/authorization/google");
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "google OAuth2 Login Redirect",null));
    }

    @Operation(summary = "회원 정보 입력 마무리")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> completeUserProfile(
            @RequestPart @Valid UserProfileRequestDto userProfileRequestDto,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage,
            HttpServletResponse response) {
        authService.completeUserProfile(userProfileRequestDto, userImage, response);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "회원 정보 입력 마무리 성공", null));
    }


    @Operation(
            summary = "Admin, test 계정 로그인"
    )
    @PostMapping("/login")
    public ResponseEntity<SingleResponse<?>> portalSignUp(@RequestBody @Valid SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response) {
//        authService.login(signUpAndLoginRequestDto, response);
        Optional<UserEntity> user = userRepository.findByEmail(signUpAndLoginRequestDto.getEmail());
        if (user.isPresent()) {
            jwtService.deleteToken(response);
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(signUpAndLoginRequestDto.getEmail(), signUpAndLoginRequestDto.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            jwtService.createToken(response);
            return ResponseEntity.ok()
                    .body(new SingleResponse<>(200, "포탈 로그인 진행 완료", "기존 유저 로그인 완료"));
        } throw new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다.");

    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        jwtService.deleteToken(response);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "로그아웃 성공", null));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        jwtService.reissueToken(request, response);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "토큰 재발급 성공", null));
    }
}
