package com.muhardin.endy.belajar.bankwebmvc.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity @Data
public class Rekening {
    @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    private String nomor;
    private String nama;
    private BigDecimal saldo;
    private Boolean aktif;
}
