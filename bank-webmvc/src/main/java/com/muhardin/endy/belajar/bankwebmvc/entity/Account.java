package com.muhardin.endy.belajar.bankwebmvc.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity @Data
public class Account {
    @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    private String accountNumber;
    private String name;
    private BigDecimal balance;
    private Boolean active;
}
