package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.config.DatabaseTestConfiguration
import com.adolfoeloy.taxtracker.product.Product
import com.adolfoeloy.taxtracker.product.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(DatabaseTestConfiguration::class)
class DatabaseIntegrationTest {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    fun `should create a new product correctly`() {
        // Arrange
        val product = Product().apply {
            name = "CDB Facil"
            certificate = "1234567890"
            issuedAt = LocalDate.of(2023, 1, 1)
            matureAt = LocalDate.of(2024, 1, 1)
        }

        // Act
        val saved = productRepository.save(product)
        val retrievedProduct = productRepository.findById(saved.id).orElse(null)

        // Assert
        assertThat(retrievedProduct).isNotNull
        assertThat(retrievedProduct?.name).isEqualTo("CDB Facil")
        assertThat(retrievedProduct?.certificate).isEqualTo("1234567890")
        assertThat(retrievedProduct?.issuedAt).isEqualTo(LocalDate.of(2023, 1, 1))
        assertThat(retrievedProduct?.matureAt).isEqualTo(LocalDate.of(2024, 1, 1))
    }
}
