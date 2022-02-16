package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.Rekening;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RekeningDaoTests {
    @Autowired private RekeningDao rekeningDao;

    @Test
    public void testFindAll(){
        Iterable<Rekening> dataRekening = rekeningDao.findAll();
        for (Rekening r : dataRekening) {
            System.out.println(r);
        }
    }
}
