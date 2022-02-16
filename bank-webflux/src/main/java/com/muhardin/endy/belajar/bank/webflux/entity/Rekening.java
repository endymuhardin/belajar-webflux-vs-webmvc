package com.muhardin.endy.belajar.bank.webflux.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
public class Rekening {
    @Id private String id;
    private String nomor;
    private String nama;
    private BigDecimal saldo;
    private Boolean aktif;
}
