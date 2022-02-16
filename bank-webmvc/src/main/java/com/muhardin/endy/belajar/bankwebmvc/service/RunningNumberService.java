package com.muhardin.endy.belajar.bankwebmvc.service;

import com.muhardin.endy.belajar.bankwebmvc.dao.RunningNumberDao;
import com.muhardin.endy.belajar.bankwebmvc.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bankwebmvc.entity.RunningNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service @Transactional
public class RunningNumberService {

    @Autowired private RunningNumberDao runningNumberDao;

    public Long ambilNomor(JenisTransaksi jenisTransaksi){
        LocalDate awalBulan = LocalDate.now().withDayOfMonth(1);
        RunningNumber runningNumber = runningNumberDao.findByJenisTransaksiAndResetPeriod(jenisTransaksi, awalBulan);
        if (runningNumber == null) {
            runningNumber = new RunningNumber();
            runningNumber.setAngkaTerakhir(0L);
            runningNumber.setResetPeriod(awalBulan);
            runningNumber.setJenisTransaksi(jenisTransaksi);
        }

        runningNumber.setAngkaTerakhir(runningNumber.getAngkaTerakhir() + 1);
        runningNumberDao.save(runningNumber);
        return runningNumber.getAngkaTerakhir();
    }
}
