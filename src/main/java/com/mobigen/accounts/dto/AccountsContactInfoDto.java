package com.mobigen.accounts.dto;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

// @ConfigurationProperties
// "/refresh"될 때 항상 Bean이 재생성된다. (destroy -> construct)

// @RefreshScope
// @RefreshScope이 붙은 Bean은 프록시(proxy)로 생성이 되고 실제 Bean을 캐쉬에 저장을 한다.
// "/refresh"될 때 Proxy의 Bean이 destroy되고 캐쉬의 값이 초기화된다. 그리고 해당 컴포넌트가 실제로 호출될 때 생성(construct)된다.
// @ConfigurationProperties로 구성된 곳에는 @RefreshScope이 필요하지 않다.
@ConfigurationProperties(prefix = "accounts")
@Getter
@Setter
public class AccountsContactInfoDto {

    private String message;
    private Map<String, String> contactDetails;
    private List<String> onCallSupport;

}
