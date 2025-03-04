package com.mobigen.accounts.controller;

import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobigen.accounts.dto.ErrorResponseDto;
import com.mobigen.accounts.service.IAccountsService;
import com.mobigen.accounts.service.client.CardsFeignClient;
import com.mobigen.accounts.utils.RequestUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RefreshScope
@Slf4j
@Tag(name = "Env check for test", description = "Env check for test")
@RestController
@RequestMapping(path = "/api/check", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
@Validated
public class EnvController {

	// @RequiredArgsConstructor + private final = @Autowired
    private final CardsFeignClient cardsFeignClient;

    @Value("${build.version}")
    private String buildVersion;

    @Autowired
    private Environment environment;
	
	@Autowired
    private IAccountsService iAccountsService;

    @Operation(summary = "Gateway check REST API", description = "REST API to check API-Gateway")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/health")
    public ResponseEntity<String> checkHealth(HttpServletRequest request,
        @RequestHeader(value = "msa-correlation-id", required = false, defaultValue = "fake-id") String customHeaderId) {
        log.info("msa-correlation-id: " + customHeaderId);
        log.info(RequestUtil.getRequestHeaderInfos(request));
        String hostname = "null";
        try {
                hostname = InetAddress.getLocalHost().getHostName().toString();
        } catch (Exception e) {
                log.error(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("HostName: " + hostname);
    }

    @Operation(summary = "Gateway check REST API", description = "REST API to check API-Gateway")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/service")
    public ResponseEntity<String> checkHealth2(HttpServletRequest request,
        @RequestHeader(value = "msa-correlation-id", required = false, defaultValue = "fake-id") String customHeaderId) {
        return cardsFeignClient.getHealthInfo();
    }



    @Operation(
            summary = "Get Build information",
            description = "Get Build information that is deployed into accounts microservice"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @GetMapping("/build-info")
    public ResponseEntity<String> getBuildInfo() {
        String hostname = "null";
        try {
			hostname = InetAddress.getLocalHost().getHostName().toString();
        } catch (Exception e) {
			log.error(e.getMessage());
        }
        log.info("Check hostname: " + hostname);
        return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(buildVersion + "::" + hostname);
    }

    @Operation(
            summary = "Get Java version",
            description = "Get Java versions details that is installed into accounts microservice"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @GetMapping("/java-version")
    public ResponseEntity<String> getJavaVersion() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(environment.getProperty("JAVA_HOME"));
    }

    @Operation(summary = "Send to communication (Test)", description = "Send to communication (Test)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status OK"),
            @ApiResponse(responseCode = "500", description = "HTTP Status Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/send")
    public ResponseEntity<String> sendCommunicationTest(
            @RequestParam(value = "param") String param) {
        boolean result = iAccountsService.sendCommunication(param);
        return ResponseEntity.status(HttpStatus.OK).body(String.valueOf(result));
    }
    
}
