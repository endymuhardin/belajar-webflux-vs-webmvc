package com.muhardin.endy.belajar.bank.webflux.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class LogTransaksi {
    @Id private String id;
    private LocalDateTime waktuAktivitas = LocalDateTime.now();
    private JenisTransaksi jenisTransaksi;
    private StatusAktivitas statusAktivitas;
    private String keterangan;
}
