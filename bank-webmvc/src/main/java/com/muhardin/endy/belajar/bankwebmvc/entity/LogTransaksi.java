package com.muhardin.endy.belajar.bankwebmvc.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Data
public class LogTransaksi {
    @Id
    @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    private LocalDateTime waktuAktivitas = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private JenisTransaksi jenisTransaksi;

    @Enumerated(EnumType.STRING)
    private StatusAktivitas statusAktivitas;
    private String keterangan;
}
