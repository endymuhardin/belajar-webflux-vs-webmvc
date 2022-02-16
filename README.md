# Komparasi aplikasi web servlet vs reactive #

Stack Web Servlet :

* Lombok
* PostgreSQL
* Spring Data JPA
* Spring WebMVC

[Klik di sini untuk generate project](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.6.3&packaging=jar&jvmVersion=11&groupId=com.muhardin.endy.belajar&artifactId=bank-webmvc&name=bank-webmvc&description=Demo%20project%20for%20Spring%20Boot&packageName=com.muhardin.endy.belajar.bank-webmvc&dependencies=lombok,postgresql,data-jpa,web)

Stack Web Reactive :

* Lombok
* PostgreSQL
* Spring Data R2DBC
* Spring WebFlux

[Klik di sini untuk generate project](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.6.3&packaging=jar&jvmVersion=11&groupId=com.muhardin.endy.belajar&artifactId=bank-webflux&name=bank-webflux&description=Demo%20project%20for%20Spring%20Boot&packageName=com.muhardin.endy.belajar.bank.webflux&dependencies=lombok,postgresql,data-r2dbc,webflux)

Poin perbandingan:

* Penggunaan class dan method
* Implementasi CRUD
* Transaction Propagation
* Transaction Isolation

Tabel database :

* Rekening
* Mutasi
* Running Number

Cara menjalankan database :

```
docker-compose up
```

Skenario yang dites dalam aplikasi:

1. Insert data berelasi normal

    * Endpoint : http://localhost:10000/api/transfer
    * Method : POST
    * Parameter :

        * src=N-002
        * dst=N-001
        * amt=50000
    
    * Response code : 201
    * Hasil yang diharapkan :
    
        * Tabel rekening di rekening asal dan tujuan berubah saldonya
        * Tabel running number angkanya naik
        * Tabel mutasi terisi record baru
        * Tabel log_transaksi terisi record baru

2. Insert data gagal

    * Endpoint : http://localhost:10000/api/transfer
    * Method : POST
    * Parameter :

        * src=N-002
        * dst=N-003
        * amt=50000
    
    * Response code : 500
    * Hasil yang diharapkan :
    
        * Tabel rekening di rekening asal dan tujuan tidak berubah saldonya (gagal update karena rollback)
        * Tabel running number angkanya tidak naik (gagal update karena rollback)
        * Tabel mutasi tidak terisi record baru (gagal insert karena rollback)
        * Tabel log_transaksi terisi record baru (tetap insert karena propagation pakai `REQUIRES_NEW`)

3. Akses data secara bersamaan (concurrent access)

    * Endpoint : http://localhost:10000/api/payment
    * Method : POST
    * Parameter :

        * src=N-002
        * prd=PLN-001
        * cst=N002
        * amt=12500
    
    * Concurrent access : 5 thread
    * Iterasi : 10
    * Response code : 201
    * Hasil yang diharapkan :
    
        * Saldo akhir di rekening N-002 berkurang sejumlah 625.000 (5x10x12500)
        * Tabel running number angkanya bertambah 50 (50 kali transaksi)
        * Tabel mutasi terisi 50 record baru dengan nomor referensi tidak ada duplikat
        * Tabel log_transaksi terisi 50 record baru