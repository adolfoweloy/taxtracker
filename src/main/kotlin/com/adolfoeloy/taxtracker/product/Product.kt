package com.adolfoeloy.taxtracker.product

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "product")
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    var name: String = ""

    @Embedded
    var certificate: Certificate = Certificate()

    @Column(name = "issued_at")
    var issuedAt: LocalDate = LocalDate.now()

    @Column(name = "mature_at")
    var matureAt: LocalDate = LocalDate.now()

}