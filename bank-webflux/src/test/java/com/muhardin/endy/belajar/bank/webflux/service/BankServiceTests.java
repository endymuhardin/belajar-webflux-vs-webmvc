package com.muhardin.endy.belajar.bank.webflux.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.stream.Stream;

@SpringBootTest
public class BankServiceTests {
    @Autowired private BankService bankService;

    @Autowired private DatabaseClient databaseClient;
    @Value("classpath:/sql/reset-data.sql") private Resource resetDatabaseScript;

    @BeforeEach
    public void resetDatabase() throws Exception {
        Stream<String> stream = new BufferedReader(new InputStreamReader(resetDatabaseScript.getInputStream())).lines();
        stream.forEach(sql -> {
            if(StringUtils.hasText(sql)) {
                databaseClient.sql(sql).then().block();
            }
        });
    }

    @Test
    public void testTransferSukses() {
        bankService.transfer("N-001", "N-002", new BigDecimal(25000)).block();
    }

    @Test
    public void testTransferRekeningTidakAktif() {
        // gagal rollback berikut lognya
        bankService.transfer("N-001", "N-003", new BigDecimal(25000)).block();
    }

    @Test
    public void testTransferSaldoKurang() {
        // ini rollback berikut lognya
        bankService.transfer("N-001", "N-002", new BigDecimal(25000000)).block();
    }
}
