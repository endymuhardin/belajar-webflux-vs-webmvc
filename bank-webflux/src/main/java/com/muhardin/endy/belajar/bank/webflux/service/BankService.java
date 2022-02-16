package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.MutasiDao;
import com.muhardin.endy.belajar.bank.webflux.dao.RekeningDao;
import com.muhardin.endy.belajar.bank.webflux.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.function.Function;

@Service @Transactional
public class BankService {

    @Autowired private RekeningDao rekeningDao;
    @Autowired private MutasiDao mutasiDao;
    @Autowired private RunningNumberService runningNumberService;
    @Autowired private LogTransaksiService logTransaksiService;

    public Mono<Void> transfer(String asal, String tujuan, BigDecimal nilai){
        Mono<LogTransaksi> logMulai = logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.MULAI,
                keteranganLogTransaksi(asal, tujuan, nilai));

        Mono<String> referensi = logMulai.then(runningNumberService.ambilNomor(JenisTransaksi.TRANSFER)
                .map(nomor -> JenisTransaksi.TRANSFER.name() + "-" + String.format("%05d",nomor)));

        // https://stackoverflow.com/a/53596358 : validasi mono
        Mono<Rekening> rekeningAsalUpdated = rekeningDao.findByNomor(asal)
                .flatMap(validasiRekening(asal, tujuan, nilai))
                .flatMap(updateSaldoRekening(nilai.negate()));

        Mono<Rekening> rekeningTujuanUpdated = rekeningDao.findByNomor(tujuan)
                .flatMap(validasiRekening(asal, tujuan, nilai))
                .flatMap(updateSaldoRekening(nilai));

        Mono<LogTransaksi> logSukses = logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.SUKSES,
                keteranganLogTransaksi(asal, tujuan, nilai));

        return Mono.zip(rekeningAsalUpdated, rekeningTujuanUpdated, referensi)
                .flatMapMany(tuple3 -> {
                    Rekening src = tuple3.getT1();
                    Rekening dst = tuple3.getT2();
                    String ref = tuple3.getT3();
                    String keterangan = keteranganTransfer(nilai, tuple3.getT1(), tuple3.getT2());
                    return Flux.concat(
                            simpanMutasi(src, keterangan, nilai.negate(), ref),
                            simpanMutasi(dst, keterangan, nilai, ref));
                })
                .flatMap(mutasiDao::save).last().then(logSukses).then();
    }

    private Function<Rekening, Mono<? extends Rekening>> validasiRekening(String asal, String tujuan, BigDecimal nilai) {
        return r -> {
            Boolean error = false;
            String errorMessage = null;

            if(saldoKurang(r, nilai)) {
                error = true;
                errorMessage = "Saldo tidak cukup";
            } else if(!r.getAktif()) {
                error = true;
                errorMessage = "Rekening tidak aktif";
            }

            if(error) {
                return logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.GAGAL,
                                keteranganLogTransaksi(asal, tujuan, nilai) +" - ["+errorMessage+"]")
                        .then(Mono.error(new IllegalStateException(errorMessage)));
            }
            return Mono.just(r);
        };
    }

    private String keteranganLogTransaksi(String asal, String tujuan, BigDecimal nilai) {
        return "Transfer "+asal+" -> "+tujuan+ " ["+nilai+"]";
    }

    private Function<Rekening, Mono<? extends Rekening>> updateSaldoRekening(BigDecimal nilai) {
        return r -> {
            r.setSaldo(r.getSaldo().add(nilai));
            return rekeningDao.save(r);
        };
    }



    private boolean saldoKurang(Rekening rekeningAsal, BigDecimal nilai) {
        return nilai.compareTo(rekeningAsal.getSaldo()) > 0;
    }

    private String keteranganTransfer(BigDecimal nilai, Rekening rekeningAsal, Rekening rekeningTujuan) {
        return "Transfer dari "+ rekeningAsal.getNomor() + " ke "+ rekeningTujuan.getNomor()+" senilai "+ nilai;
    }

    private Mono<Mutasi> simpanMutasi(Rekening rekening, String keterangan, BigDecimal nilai, String referensi) {
        Mutasi mutasi = new Mutasi();
        mutasi.setRekening(rekening);
        mutasi.setIdRekening(rekening.getId());
        mutasi.setJenisTransaksi(JenisTransaksi.TRANSFER);
        mutasi.setKeterangan(keterangan);
        mutasi.setNilai(nilai);
        mutasi.setReferensi(referensi+"-"+rekening.getNomor());
        return mutasiDao.save(mutasi);
    }
}
