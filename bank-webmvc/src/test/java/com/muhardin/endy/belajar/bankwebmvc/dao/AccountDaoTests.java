package com.muhardin.endy.belajar.bankwebmvc.dao;

import com.muhardin.endy.belajar.bankwebmvc.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AccountDaoTests {
    @Autowired private AccountDao accountDao;

    @Test
    public void testFindAll(){
        Iterable<Account> dataRekening = accountDao.findAll();
        for (Account r : dataRekening) {
            System.out.println(r);
        }
    }
}
