package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bankwebmvc.entity.RunningNumber;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.LockModeType;
import java.time.LocalDate;

public interface RunningNumberDao extends PagingAndSortingRepository<RunningNumber, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    RunningNumber findByJenisTransaksiAndResetPeriod(JenisTransaksi jenisTransaksi, LocalDate awalBulan);
}
