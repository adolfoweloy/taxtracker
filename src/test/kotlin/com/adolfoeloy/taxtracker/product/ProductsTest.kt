package com.adolfoeloy.taxtracker.product

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductsTest {

    @Nested
    @DisplayName("createProduct tests")
    inner class CreateProductTests {

        @Test
        @DisplayName("should create product with valid data and correct date formatting")
        fun shouldCreateProductWithValidDataAndCorrectDateFormatting() {
            // Given
            val name = "Investment Fund XYZ"
            val certificate = Certificate.createNormalizedCertificate("12602370000023458961")
            val issuedAt = "15/03/2023"
            val matureAt = "15/03/2025"

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product).isNotNull
            assertThat(product.name).isEqualTo(name)
            assertThat(product.certificate).isEqualTo(certificate)
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2023, 3, 15))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2025, 3, 15))
            assertThat(product.id).isEqualTo(0) // Default value before persistence
        }

        @Test
        @DisplayName("should handle leap year dates correctly")
        fun shouldHandleLeapYearDatesCorrectly() {
            // Given
            val name = "Leap Year Product"
            val certificate = Certificate.createNormalizedCertificate("12602370000023456789")
            val issuedAt = "29/02/2024" // Leap year
            val matureAt = "28/02/2025" // Non-leap year

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2024, 2, 29))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2025, 2, 28))
        }

        @Test
        @DisplayName("should create product with single digit day and month")
        fun shouldCreateProductWithSingleDigitDayAndMonth() {
            // Given
            val name = "Single Digit Date Product"
            val certificate = Certificate.createNormalizedCertificate("12602370000023457890")
            val issuedAt = "05/07/2023"
            val matureAt = "09/11/2025"

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2023, 7, 5))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2025, 11, 9))
        }


        @Test
        @DisplayName("should handle maturity date before issued date")
        fun shouldHandleMaturityDateBeforeIssuedDate() {
            // Given
            val name = "Reverse Date Product"
            val certificate = Certificate.createNormalizedCertificate("12602370000023458765")
            val issuedAt = "15/12/2024"
            val matureAt = "15/06/2024" // Before issued date

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2024, 12, 15))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2024, 6, 15))
            assertThat(product.matureAt).isBefore(product.issuedAt)
        }

        @Test
        @DisplayName("should throw exception for invalid date format")
        fun shouldThrowExceptionForInvalidDateFormat() {
            // Given
            val name = "Invalid Date Product"
            val certificate = Certificate.createNormalizedCertificate("12602370000023458999")
            val invalidIssuedAt = "2023-03-15" // Wrong format (should be dd/MM/yyyy)
            val validMatureAt = "15/03/2025"

            // When & Then
            assertThatThrownBy {
                Products.createProduct(name, certificate, invalidIssuedAt, validMatureAt)
            }.hasMessageContaining("could not be parsed")
        }

        @Test
        @DisplayName("should throw exception for invalid mature date format")
        fun shouldThrowExceptionForInvalidMatureDateFormat() {
            // Given
            val name = "Invalid Mature Date Product"
            val certificate = Certificate.createNormalizedCertificate("12602370000023458888")
            val validIssuedAt = "15/03/2023"
            val invalidMatureAt = "March 15, 2025" // Wrong format

            // When & Then
            assertThatThrownBy {
                Products.createProduct(name, certificate, validIssuedAt, invalidMatureAt)
            }.hasMessageContaining("could not be parsed")
        }


        @Test
        @DisplayName("should create product with empty name and certificate")
        fun shouldCreateProductWithEmptyNameAndCertificate() {
            // Given
            val name = ""
            val certificate = Certificate.createNormalizedCertificate("")
            val issuedAt = "01/01/2023"
            val matureAt = "01/01/2024"

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product.name).isEmpty()
            assertThat(product.certificate.value).isEmpty()
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2023, 1, 1))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2024, 1, 1))
        }

        @Test
        @DisplayName("should create product with same issued and mature dates")
        fun shouldCreateProductWithSameIssuedAndMatureDates() {
            // Given
            val name = "Same Date Product"
            val certificate = Certificate.createNormalizedCertificate("12602370000023458666")
            val date = "15/08/2024"

            // When
            val product = Products.createProduct(name, certificate, date, date)

            // Then
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2024, 8, 15))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2024, 8, 15))
            assertThat(product.issuedAt).isEqualTo(product.matureAt)
        }
    }
}