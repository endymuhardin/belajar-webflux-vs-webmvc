package com.muhardin.endy.belajar.bank.webflux.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Stream;

@DataR2dbcTest
public class RekeningDaoTests {
    @Autowired private RekeningDao rekeningDao;
    @Autowired private DatabaseClient databaseClient;
    @Value("classpath:/sql/reset-data.sql") private Resource resetDatabaseScript;

    @BeforeEach
    public void resetDatabase() throws Exception {
        Stream<String> stream = new BufferedReader(new InputStreamReader(resetDatabaseScript.getInputStream())).lines();
        stream.forEach(sql -> {
            if(StringUtils.hasText(sql)) {
                databaseClient.sql(sql).then().block();
            }
        });
    }

    @Test
    public void testFindAll() throws Exception {
        rekeningDao.findAll().doOnNext(System.out::println).blockLast();

    }
}
