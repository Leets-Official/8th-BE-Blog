package org.example._thbe.domain.member.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 20, message = "이름은 20자 이하로 입력해주세요.")
        String name,
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요.")
        String password,
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email) {}

