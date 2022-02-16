package com.muhardin.endy.belajar.bankwebmvc.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

@SpringBootTest
@Sql(scripts = {"/sql/reset-data.sql"})
public class BankServiceTests {
    @Autowired private BankService bankService;

    @Test
    public void testTransferSukses() {
        bankService.transfer("N-001", "N-002", new BigDecimal(25000));
    }

    @Test
    public void testTransferRekeningTidakAktif() {
        bankService.transfer("N-001", "N-003", new BigDecimal(25000));
    }

    @Test
    public void testTransferSaldoKurang() {
        bankService.transfer("N-001", "N-002", new BigDecimal(25000000));
    }
}
