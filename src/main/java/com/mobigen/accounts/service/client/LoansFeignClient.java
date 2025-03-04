package com.mobigen.accounts.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.mobigen.accounts.dto.LoansDto;

@FeignClient(name = "loans", path = "/api", fallback = LoansFallback.class)
public interface LoansFeignClient {

    @GetMapping(value = "/api/fetch", consumes = "application/json")
    public ResponseEntity<LoansDto> fetchLoanDetails(
        @RequestHeader("msa-correlation-id") String correlationId,
        @RequestParam(value = "mobileNumber") String mobileNumber);

}
