package inu.codin.codin.domain.info.domain.office.entity;

import inu.codin.codin.domain.info.domain.office.dto.OfficeMemberRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/*
    학과 사무실 직원 정보
 */
@Getter
public class OfficeMember {
    @NotBlank
    @Schema(description = "성명", example = "홍길동")
    private String name;
    @NotBlank
    @Schema(description = "직위", example = "조교")
    private String position;

    @Schema(description = "담당 업무", example = "학과사무실 업무")
    private String role;

    @NotBlank
    @Schema(description = "연락처", example = "032-123-2345")
    private String number;

    @NotBlank
    @Schema(description = "이메일", example = "test@inu.ac.kr")
    private String email;

    public void update(OfficeMemberRequestDto officeMemberRequestDto) {
        this.name = officeMemberRequestDto.getName();
        this.position = officeMemberRequestDto.getPosition();
        this.role = officeMemberRequestDto.getRole();
        this.number = officeMemberRequestDto.getNumber();
        this.email = officeMemberRequestDto.getEmail();
    }
}
