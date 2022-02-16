package com.muhardin.endy.belajar.bank.webflux.dao;

import com.muhardin.endy.belajar.bank.webflux.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bank.webflux.entity.RunningNumber;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDate;

public interface RunningNumberDao extends ReactiveCrudRepository<RunningNumber, String> {
    RunningNumber findByJenisTransaksiAndResetPeriod(JenisTransaksi jenisTransaksi, LocalDate awalBulan);
}
