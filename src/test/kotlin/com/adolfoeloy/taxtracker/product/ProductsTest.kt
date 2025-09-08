package com.adolfoeloy.taxtracker.product

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductsTest {

    @Nested
    @DisplayName("normalizeCertificate tests")
    inner class NormalizeCertificateTests {

        @Test
        @DisplayName("should normalize 13-character certificate to 20-character format")
        fun shouldNormalize13CharacterCertificateTo20CharacterFormat() {
            // Given
            val name = "Product with short certificate"
            val shortCertificate = "1260023458960" // 13 characters
            val expectedLongCertificate = "12602370000023458960" // 20 characters
            val issuedAt = "01/11/2023"
            val matureAt = "01/11/2024"

            // When
            val product = Products.createProduct(name, shortCertificate, issuedAt, matureAt)

            // Then
            assertThat(product.certificate).isEqualTo(expectedLongCertificate)
            assertThat(product.name).isEqualTo(name)
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2023, 11, 1))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2024, 11, 1))
        }

        @Test
        @DisplayName("should handle both short and long certificate formats for same product")
        fun shouldHandleBothShortAndLongCertificateFormatsForSameProduct() {
            // Given
            val shortCertificate = "1260023458960"
            val longCertificate = "12602370000023458960"
            val commonDate = "15/11/2023"

            // When
            val productFromShort = Products.createProduct("Product Short", shortCertificate, commonDate, commonDate)
            val productFromLong = Products.createProduct("Product Long", longCertificate, commonDate, commonDate)

            // Then
            assertThat(productFromShort.certificate).isEqualTo(productFromLong.certificate)
            assertThat(productFromShort.certificate).isEqualTo(longCertificate)
        }

        @Test
        @DisplayName("should normalize multiple 13-character certificates correctly")
        fun shouldNormalizeMultiple13CharacterCertificatesCorrectly() {
            // Given
            val testCases = mapOf(
                "1260023458960" to "12602370000023458960",
                "1260023458961" to "12602370000023458961",
                "1260023458962" to "12602370000023458962",
                "1260023459123" to "12602370000023459123",
                "1260023456789" to "12602370000023456789"
            )

            testCases.forEach { (shortCert, expectedLongCert) ->
                // When
                val product = Products.createProduct(
                    "Test Product",
                    shortCert,
                    "01/01/2023",
                    "01/01/2024"
                )

                // Then
                assertThat(product.certificate)
                    .withFailMessage("Short certificate $shortCert should normalize to $expectedLongCert")
                    .isEqualTo(expectedLongCert)
            }
        }

        @Test
        @DisplayName("should keep 20-character certificates unchanged")
        fun shouldKeep20CharacterCertificatesUnchanged() {
            // Given
            val longCertificate = "12602370000023458960"
            val name = "Product with long certificate"
            val issuedAt = "01/01/2023"
            val matureAt = "01/01/2024"

            // When
            val product = Products.createProduct(name, longCertificate, issuedAt, matureAt)

            // Then
            assertThat(product.certificate).isEqualTo(longCertificate)
        }

        @Test
        @DisplayName("should handle certificates with other lengths unchanged")
        fun shouldHandleCertificatesWithOtherLengthsUnchanged() {
            // Given
            val testCases = listOf(
                "123456789", // 9 characters
                "1234567890123456", // 16 characters
                "123456789012345678901", // 21 characters
                "12", // 2 characters
                "" // Empty string
            )

            testCases.forEach { certificate ->
                // When
                val product = Products.createProduct(
                    "Test Product",
                    certificate,
                    "01/01/2023",
                    "01/01/2024"
                )

                // Then
                assertThat(product.certificate)
                    .withFailMessage("Certificate $certificate with length ${certificate.length} should remain unchanged")
                    .isEqualTo(certificate)
            }
        }

        @Test
        @DisplayName("should create product with different certificate number format")
        fun shouldCreateProductWithDifferentCertificateNumberFormat() {
            // Given
            val name = "Bond ABC"
            val certificate = "12602370000023459123"
            val issuedAt = "01/01/2024"
            val matureAt = "31/12/2024"

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product.certificate).isEqualTo(certificate)
            assertThat(product.name).isEqualTo(name)
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2024, 1, 1))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2024, 12, 31))
        }
    }

    @Nested
    @DisplayName("createProduct tests")
    inner class CreateProductTests {

        @Test
        @DisplayName("should create product with valid data and correct date formatting")
        fun shouldCreateProductWithValidDataAndCorrectDateFormatting() {
            // Given
            val name = "Investment Fund XYZ"
            val certificate = "12602370000023458961"
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
            val certificate = "12602370000023456789"
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
            val certificate = "12602370000023457890"
            val issuedAt = "05/07/2023"
            val matureAt = "09/11/2025"

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2023, 7, 5))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2025, 11, 9))
        }

        @Test
        @DisplayName("should create product with various certificate number patterns")
        fun shouldCreateProductWithVariousCertificateNumberPatterns() {
            // Given
            val testCases = listOf(
                "12602370000023451111",
                "12602370000023452222",
                "12602370000023453333",
                "12602370000023454444",
                "12602370000023455555"
            )

            testCases.forEachIndexed { index, certificate ->
                // When
                val product = Products.createProduct(
                    "Product $index",
                    certificate,
                    "01/01/2023",
                    "01/01/2024"
                )

                // Then
                assertThat(product.certificate).isEqualTo(certificate)
                assertThat(product.name).isEqualTo("Product $index")
            }
        }

        @Test
        @DisplayName("should handle maturity date before issued date")
        fun shouldHandleMaturityDateBeforeIssuedDate() {
            // Given
            val name = "Reverse Date Product"
            val certificate = "12602370000023458765"
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
            val certificate = "12602370000023458999"
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
            val certificate = "12602370000023458888"
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
            val certificate = ""
            val issuedAt = "01/01/2023"
            val matureAt = "01/01/2024"

            // When
            val product = Products.createProduct(name, certificate, issuedAt, matureAt)

            // Then
            assertThat(product.name).isEmpty()
            assertThat(product.certificate).isEmpty()
            assertThat(product.issuedAt).isEqualTo(LocalDate.of(2023, 1, 1))
            assertThat(product.matureAt).isEqualTo(LocalDate.of(2024, 1, 1))
        }

        @Test
        @DisplayName("should create product with same issued and mature dates")
        fun shouldCreateProductWithSameIssuedAndMatureDates() {
            // Given
            val name = "Same Date Product"
            val certificate = "12602370000023458666"
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