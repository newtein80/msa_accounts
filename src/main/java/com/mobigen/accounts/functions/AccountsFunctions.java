package com.mobigen.accounts.functions;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mobigen.accounts.service.IAccountsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AccountsFunctions {
    @Bean
    public Consumer<Long> updateCommunication(IAccountsService accountsService) {
        return accountNumber -> {
            log.info("Updating Communication status for the account number : " + accountNumber.toString());
            accountsService.updateCommunicationStatus(accountNumber);
        };
    }
}
