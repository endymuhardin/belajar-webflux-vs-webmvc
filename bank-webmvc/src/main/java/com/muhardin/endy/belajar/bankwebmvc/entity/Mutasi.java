package com.muhardin.endy.belajar.bankwebmvc.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Data
public class Mutasi {
    @Id
    @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    @Enumerated(EnumType.STRING)
    private JenisTransaksi jenisTransaksi;

    @ManyToOne @JoinColumn(name = "id_rekening")
    private Rekening rekening;
    private LocalDateTime waktuTransaksi = LocalDateTime.now();
    private BigDecimal nilai;
    private String keterangan;
    private String referensi;
}
