package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.RunningNumberDao;
import com.muhardin.endy.belajar.bank.webflux.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bank.webflux.entity.RunningNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service @Transactional
public class RunningNumberService {
    @Autowired
    private RunningNumberDao runningNumberDao;

    @Autowired
    private R2dbcEntityTemplate template;

    public Mono<Long> ambilNomor(JenisTransaksi jenisTransaksi){
        LocalDate awalBulan = LocalDate.now().withDayOfMonth(1);
        return runningNumberDao.findByJenisTransaksiAndResetPeriod(jenisTransaksi, awalBulan)
                .defaultIfEmpty(createRunningNumber(jenisTransaksi, awalBulan))
                .map(r -> {
                    r.setAngkaTerakhir(r.getAngkaTerakhir() + 1);
                    return r;
                })
                .flatMap(runningNumberDao::save)
                .map(r -> r.getAngkaTerakhir());
    }

    private RunningNumber createRunningNumber(JenisTransaksi jenisTransaksi, LocalDate resetPeriod){
        RunningNumber runningNumber = new RunningNumber();
        runningNumber.setAngkaTerakhir(0L);
        runningNumber.setResetPeriod(resetPeriod);
        runningNumber.setJenisTransaksi(jenisTransaksi);
        return runningNumber;
    }
}
