package com.muhardin.endy.belajar.bank.webflux.service;

import com.muhardin.endy.belajar.bank.webflux.dao.MutasiDao;
import com.muhardin.endy.belajar.bank.webflux.dao.RekeningDao;
import com.muhardin.endy.belajar.bank.webflux.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.util.function.Function;

@Service @Transactional
public class BankService {

    @Autowired private RekeningDao rekeningDao;
    @Autowired private MutasiDao mutasiDao;
    @Autowired private RunningNumberService runningNumberService;
    @Autowired private LogTransaksiService logTransaksiService;

    public Mono<Void> transfer(String asal, String tujuan, BigDecimal nilai){
        Mono<String> referensi = logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.MULAI,
                        keteranganTransfer(asal, tujuan, nilai))
                .then(runningNumberService.ambilNomor(JenisTransaksi.TRANSFER)
                .map(nomor -> JenisTransaksi.TRANSFER.name() + "-" + String.format("%05d",nomor)));

        // https://stackoverflow.com/a/53596358 : validasi mono
        Mono<Rekening> rekeningAsal = rekeningDao.findByNomor(asal)
                .flatMap(validasiRekening(nilai))
                .onErrorMap(handleErrorValidasi(asal, tujuan, nilai));

        Mono<Rekening> rekeningTujuan = rekeningDao.findByNomor(tujuan)
                .flatMap(validasiRekening(nilai))
                .onErrorMap(handleErrorValidasi(asal, tujuan, nilai));

        Mono<LogTransaksi> logSukses = logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.SUKSES,
                keteranganTransfer(asal, tujuan, nilai));

        return
            Mono.zip(rekeningAsal, rekeningTujuan, referensi)
            .flatMapMany(tuple3 -> {
                Rekening src = tuple3.getT1();

                Rekening dst = tuple3.getT2();
                String ref = tuple3.getT3();
                String keterangan = keteranganTransfer(asal, tujuan, nilai);

                src.setSaldo(src.getSaldo().subtract(nilai));
                dst.setSaldo(dst.getSaldo().add(nilai));

                return
                    rekeningDao.save(src)
                    .then(rekeningDao.save(dst))
                    .thenMany(
                        Flux.concat(
                        simpanMutasi(src, keterangan, nilai.negate(), ref),
                        simpanMutasi(dst, keterangan, nilai, ref))
                    );
            })
            .flatMap(mutasiDao::save).last().then(logSukses).then();
    }

    private Function<Tuple2<Rekening, String>, Mono<? extends Mutasi>> mutasiRekening(String asal, String tujuan, BigDecimal nilai) {
        return tuple2-> simpanMutasi(tuple2.getT1(), keteranganTransfer(asal, tujuan, nilai), nilai, tuple2.getT2());
    }

    private Function<Rekening, Mono<? extends Rekening>> updateSaldo(BigDecimal nilai) {
        return r -> {
            r.setSaldo(r.getSaldo().add(nilai));
            return rekeningDao.save(r);
        };
    }

    private Function<Throwable, Throwable> handleErrorValidasi(String asal, String tujuan, BigDecimal nilai) {
        return e -> logTransaksiService.catat(JenisTransaksi.TRANSFER, StatusAktivitas.GAGAL,
                    keteranganTransfer(asal, tujuan, nilai) +" - ["+e.getMessage()+"]")
                    .as(t -> e);
    }

    private Function<Rekening, Mono<? extends Rekening>> validasiRekening(BigDecimal nilai) {
        return r -> {
            if(saldoKurang(r, nilai)) {
                return Mono.error(new IllegalStateException("Saldo tidak cukup"));
            } else if(!r.getAktif()) {
                return Mono.error(new IllegalStateException("Rekening tidak aktif"));
            }
            return Mono.just(r);
        };
    }

    private String keteranganTransfer(String asal, String tujuan, BigDecimal nilai) {
        return "Transfer "+asal+" -> "+tujuan+ " ["+nilai+"]";
    }

    private boolean saldoKurang(Rekening rekeningAsal, BigDecimal nilai) {
        return nilai.compareTo(rekeningAsal.getSaldo()) > 0;
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
