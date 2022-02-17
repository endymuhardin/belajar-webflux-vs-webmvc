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
    public void testTransferSukses() {
        StepVerifier.create(
            bankService.transfer("N-001", "N-002", new BigDecimal(25000))
        ).verifyComplete();
    }

    // Verifikasi error :
    // select * from running_number : harusnya rollback
    // select * from mutasi : harusnya rollback
    // select * from rekening : harusnya rollback
    // select * from log_transaksi : harusnya tidak rollback (error log tercatat)
    // hasil method : throw Exception

    @Test
    public void testTransferRekeningTidakAktif() {
        StepVerifier.create(
            bankService.transfer("N-001", "N-003", new BigDecimal(25000))
        ).verifyError();
    }

    @Test
    public void testTransferSaldoKurang() {
        StepVerifier.create(
                bankService.transfer("N-001", "N-002", new BigDecimal(25000000))
        ).verifyError();
    }
}
