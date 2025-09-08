package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.product.Certificate
import com.adolfoeloy.taxtracker.product.Product
import com.adolfoeloy.taxtracker.product.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class ProductRepositoryIT : AbstractDatabaseIntegrationTest() {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    fun `should create a new product correctly`() {
        // Arrange
        val normalizedCertificate = Certificate.createNormalizedCertificate("1234567890")
        val product = Product().apply {
            name = "CDB Facil"
            certificate = normalizedCertificate
            issuedAt = LocalDate.of(2023, 1, 1)
            matureAt = LocalDate.of(2024, 1, 1)
        }

        // Act
        val saved = productRepository.save(product)
        val retrievedProduct = productRepository.findById(saved.id).orElse(null)

        // Assert
        assertThat(retrievedProduct).isNotNull
        assertThat(retrievedProduct?.name).isEqualTo("CDB Facil")
        assertThat(retrievedProduct?.certificate).isEqualTo(normalizedCertificate)
        assertThat(retrievedProduct?.issuedAt).isEqualTo(LocalDate.of(2023, 1, 1))
        assertThat(retrievedProduct?.matureAt).isEqualTo(LocalDate.of(2024, 1, 1))
    }
}
