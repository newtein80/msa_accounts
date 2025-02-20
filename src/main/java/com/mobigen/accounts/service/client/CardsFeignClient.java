package com.mobigen.accounts.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cards", path = "/api")
public interface CardsFeignClient {

    @GetMapping(value = "/health",consumes = "application/json")
    public ResponseEntity<String> fetchCardDetails();

}