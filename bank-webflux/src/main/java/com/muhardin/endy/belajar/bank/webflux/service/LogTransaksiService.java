package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.LogTransaksiDao;
import com.muhardin.endy.belajar.bank.webflux.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bank.webflux.entity.LogTransaksi;
import com.muhardin.endy.belajar.bank.webflux.entity.StatusAktivitas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class LogTransaksiService {

    @Autowired
    private LogTransaksiDao logTransaksiDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Mono<LogTransaksi> catat(JenisTransaksi jenisTransaksi, StatusAktivitas statusAktivitas, String keterangan){
        LogTransaksi logTransaksi = new LogTransaksi();
        logTransaksi.setJenisTransaksi(jenisTransaksi);
        logTransaksi.setStatusAktivitas(statusAktivitas);
        logTransaksi.setKeterangan(keterangan);
        return logTransaksiDao.save(logTransaksi);
    }
}
