package com.mobigen.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(
        name = "Customer",
        description = "Schema to hold Customer and Account information"
) // swagger 에서 표시될 CustomerDto 클래스의 정보를 작성
public class CustomerDto {

    @Schema(
            description = "Name of the customer", example = "Eazy Bytes"
    ) // swagger 에서 표시될 CustomerDto 클래스의 name 필드의 내용을 작성
    @NotEmpty(message = "Name can not be a null or empty") // Null이나 빈 문자열 등 비어있는 상태를 체크 (길이를 체크할 수 있는 타입(String, Collection 등)만 사용 가능). 그외 @NotBlank (Null 여부와 공백 여부를 체크합니다. 문자열 타입만 사용)도 존재
    @Size(min = 5, max = 30, message = "The length of the customer name should be between 5 and 30") // 필드의 크기에 검증하기위해 사용
    private String name;

    @Schema(
            description = "Email address of the customer", example = "tutor@eazybytes.com"
    )
    @NotEmpty(message = "Email address can not be a null or empty")
    @Email(message = "Email address should be a valid value") // Email 형식 유효성 체크
    private String email;

    @Schema(
            description = "Mobile Number of the customer", example = "9345432123"
    )
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits") // 정규식을 사용한 패턴 매칭
    private String mobileNumber;

    @Schema(
            description = "Account details of the Customer"
    )
    private AccountsDto accountsDto;
}
