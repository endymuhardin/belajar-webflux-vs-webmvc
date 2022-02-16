package com.muhardin.endy.belajar.bankwebmvc.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@Entity @Data
public class RunningNumber {
    @Id
    @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    @Enumerated(EnumType.STRING)
    private JenisTransaksi jenisTransaksi;
    private LocalDate resetPeriod = LocalDate.now();
    private Long angkaTerakhir;
}
