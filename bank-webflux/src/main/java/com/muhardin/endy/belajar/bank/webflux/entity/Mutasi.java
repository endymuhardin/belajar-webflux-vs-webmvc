package com.muhardin.endy.belajar.bank.webflux.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Mutasi {
    @Id private String id;
    private JenisTransaksi jenisTransaksi;

    @Transient
    private Rekening rekening;
    private String idRekening;
    private LocalDateTime waktuTransaksi = LocalDateTime.now();
    private BigDecimal nilai;
    private String keterangan;
    private String referensi;
}
