package com.muhardin.endy.belajar.bankwebmvc.service;

import com.muhardin.endy.belajar.bankwebmvc.dao.MutasiDao;
import com.muhardin.endy.belajar.bankwebmvc.dao.RekeningDao;
import com.muhardin.endy.belajar.bankwebmvc.entity.JenisTransaksi;
import com.muhardin.endy.belajar.bankwebmvc.entity.Mutasi;
import com.muhardin.endy.belajar.bankwebmvc.entity.Rekening;
import com.muhardin.endy.belajar.bankwebmvc.entity.StatusAktivitas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service @Transactional
public class BankService {

    @Autowired private RekeningDao rekeningDao;
    @Autowired private MutasiDao mutasiDao;

    @Autowired private RunningNumberService runningNumberService;
    @Autowired private LogTransaksiService logTransaksiService;

    public void transfer(String asal, String tujuan, BigDecimal nilai){

        logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.MULAI,
                "Transfer "+asal+" -> "+tujuan+ "["+nilai+"]");

        String referensi = JenisTransaksi.TRANSFER.name()
                + "-" + String.format("%05d",
                runningNumberService.ambilNomor(JenisTransaksi.TRANSFER));

        Rekening rekeningAsal = rekeningDao.findByNomor(asal);
        Rekening rekeningTujuan = rekeningDao.findByNomor(tujuan);

        if(rekeningTidakAktif(rekeningAsal, rekeningTujuan)) {
            logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.GAGAL,
                    "Transfer "+asal+" -> "+tujuan+ "["+nilai+"] - [REKENING TIDAK AKTIF]");
            throw new IllegalArgumentException("Rekening tidak aktif");
        }

        if (saldoKurang(rekeningAsal, nilai)) {
            logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.GAGAL,
                    "Transfer "+asal+" -> "+tujuan+ "["+nilai+"] - [SALDO KURANG]");
            throw new IllegalStateException("Saldo tidak cukup");
        }

        simpanMutasiTransfer(rekeningAsal, rekeningTujuan, nilai, referensi);
        updateSaldoRekeningTransfer(rekeningAsal, rekeningTujuan, nilai);

        logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.SUKSES,
                "Transfer "+asal+" -> "+tujuan+ "["+nilai+"]");
    }

    private void simpanMutasiTransfer(Rekening rekeningAsal, Rekening rekeningTujuan, BigDecimal nilai, String referensi) {
        String keterangan = keteranganTransfer(nilai, rekeningAsal, rekeningTujuan);
        simpanMutasi(rekeningAsal, keterangan, nilai.negate(), referensi);
        simpanMutasi(rekeningTujuan, keterangan, nilai, referensi);
    }

    private void updateSaldoRekeningTransfer(Rekening rekeningAsal, Rekening rekeningTujuan, BigDecimal nilai) {
        rekeningAsal.setSaldo(rekeningAsal.getSaldo().subtract(nilai));
        rekeningTujuan.setSaldo(rekeningTujuan.getSaldo().add(nilai));
        rekeningDao.save(rekeningAsal);
        rekeningDao.save(rekeningTujuan);
    }

    private void simpanMutasi(Rekening rekening, String keterangan, BigDecimal nilai, String referensi) {
        Mutasi mutasi = new Mutasi();
        mutasi.setRekening(rekening);
        mutasi.setJenisTransaksi(JenisTransaksi.TRANSFER);
        mutasi.setKeterangan(keterangan);
        mutasi.setNilai(nilai.negate());
        mutasi.setReferensi(referensi+"-"+rekening.getNomor());
        mutasiDao.save(mutasi);
    }

    private String keteranganTransfer(BigDecimal nilai, Rekening rekeningAsal, Rekening rekeningTujuan) {
        return "Transfer dari "+ rekeningAsal.getNomor() + " ke "+ rekeningTujuan.getNomor()+" senilai "+ nilai;
    }

    private boolean saldoKurang(Rekening rekeningAsal, BigDecimal nilai) {
        return nilai.compareTo(rekeningAsal.getSaldo()) > 0;
    }

    private boolean rekeningTidakAktif(Rekening rekeningAsal, Rekening rekeningTujuan) {
        return !(rekeningAsal.getAktif() && rekeningTujuan.getAktif());
    }
}
