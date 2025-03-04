package com.mobigen.accounts.service.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.mobigen.accounts.dto.LoansDto;

@Component
public class LoansFallback implements LoansFeignClient {

    @Override
    public ResponseEntity<LoansDto> fetchLoanDetails(String correlationId, String mobileNumber) {
        return ResponseEntity
				.status(HttpStatus.REQUEST_TIMEOUT)
				.body(null);
    }
    
}
