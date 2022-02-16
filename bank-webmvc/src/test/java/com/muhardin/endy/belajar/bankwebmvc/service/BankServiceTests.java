package com.muhardin.endy.belajar.bankwebmvc.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest @Slf4j
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

    @Test
    public void testMultithreading() throws Exception {
        // inisialisasi dulu running numbernya
        bankService.transfer("N-001", "N-002", new BigDecimal(25000));
        Thread.sleep(1000);

        int numThread = 5;
        final int numLoop = 20;

        final List<TransferThread> transferThreadList =
                Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numThread; i++) {
            transferThreadList.add(new TransferThread(bankService, numLoop, i));
        }

        log.info("Start {} thread, masing-masing {} iterasi", numThread, numLoop);
        // run threads
        for (TransferThread tt : transferThreadList) {
            new Thread(tt).start();
            Thread.sleep(10);
        }

        // check thread completion
        boolean completed = false;
        while(!completed){
            for (TransferThread tt : transferThreadList) {
                completed = tt.getCompleted();
                System.out.println("Thread : "+tt.getThreadNumber() +" "+(completed?"completed":"still running"));
            }

            Thread.sleep(10);
        }

        log.info("Selesai menjalankan test multithreading");
    }
}

@Getter
class TransferThread implements Runnable {

    private BankService bankService;
    private final Integer iteration;
    private final Integer threadNumber;
    private Boolean completed = false;

    public TransferThread(BankService service, Integer numIteration, Integer threadNumber) {
        this.bankService = service;
        this.iteration = numIteration;
        this.threadNumber = threadNumber;
    }

    @Override
    public void run() {
        for (int i = 0; i < iteration; i++) {
            System.out.println("Thread " + threadNumber + " : iterasi : " + i);
            bankService.transfer("N-001", "N-002", new BigDecimal(1000));
        }
        completed = true;
    }
}
