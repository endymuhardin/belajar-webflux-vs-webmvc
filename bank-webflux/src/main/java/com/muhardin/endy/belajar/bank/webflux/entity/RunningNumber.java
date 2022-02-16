package com.muhardin.endy.belajar.bank.webflux.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
public class RunningNumber {
    @Id private String id;
    private JenisTransaksi jenisTransaksi;
    private LocalDate resetPeriod = LocalDate.now();
    private Long angkaTerakhir;
}
