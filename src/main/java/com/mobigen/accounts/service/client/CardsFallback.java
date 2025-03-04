package com.mobigen.accounts.service.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.mobigen.accounts.dto.CardsDto;

@Component
public class CardsFallback implements CardsFeignClient {

    @Override
    public ResponseEntity<CardsDto> fetchCardDetails(String correlationId, String mobileNumber) {
        return ResponseEntity
				.status(HttpStatus.REQUEST_TIMEOUT)
				.body(null);
    }

    @Override
    public ResponseEntity<String> getHealthInfo() {
        return ResponseEntity
				.status(HttpStatus.REQUEST_TIMEOUT)
				.body("Fallback...");
    }
    
}
