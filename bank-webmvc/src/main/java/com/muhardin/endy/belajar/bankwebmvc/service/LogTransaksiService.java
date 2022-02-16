package com.muhardin.endy.belajar.bankwebmvc.service;

import com.muhardin.endy.belajar.bankwebmvc.dao.LogTransaksiDao;
import com.muhardin.endy.belajar.bankwebmvc.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bankwebmvc.entity.LogTransaksi;
import com.muhardin.endy.belajar.bankwebmvc.entity.StatusAktivitas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogTransaksiService {

    @Autowired private LogTransaksiDao logTransaksiDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void catat(JenisTransaksi jenisTransaksi, StatusAktivitas statusAktivitas, String keterangan){
        LogTransaksi logTransaksi = new LogTransaksi();
        logTransaksi.setJenisTransaksi(jenisTransaksi);
        logTransaksi.setStatusAktivitas(statusAktivitas);
        logTransaksi.setKeterangan(keterangan);
        logTransaksiDao.save(logTransaksi);
    }
}
