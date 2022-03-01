package com.muhardin.endy.belajar.bank.webflux.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@SpringBootTest
public class BankServiceTests {
    @Autowired private BankService bankService;
    @Autowired private R2dbcEntityTemplate entityTemplate;
    @Value("classpath:/sql/reset-data.sql") private Resource resetDatabaseScript;

    @BeforeEach
    public void resetDatabase() throws Exception {
        String query = StreamUtils.copyToString(resetDatabaseScript.getInputStream(), StandardCharsets.UTF_8);
        Mono<Void> mono = this.entityTemplate
                .getDatabaseClient()
                .sql(query)
                .then();

        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    public void testTransferSuccess() {
        StepVerifier.create(
            bankService.transfer("C-001", "C-002", new BigDecimal(25000))
        ).verifyComplete();
    }

    // Expected result :
    // select * from running_number : should rollback
    // select * from transaction_history : should rollback
    // select * from account : should rollback
    // select * from transaction_log : *should not* rollback (error log inserted into database)
    // method outcome : throw Exception

    @Test
    public void testTransferInactiveAccount() {
        StepVerifier.create(
            bankService.transfer("C-001", "C-003", new BigDecimal(25000))
        ).verifyError();
    }

    @Test
    public void testTransferInsufficientBalance() {
        StepVerifier.create(
                bankService.transfer("C-001", "C-002", new BigDecimal(25000000))
        ).verifyError();
    }
}
