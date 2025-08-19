package com.adolfoeloy.taxtracker.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Int> {

    fun findByCertificate(certificate: String): Product?

}