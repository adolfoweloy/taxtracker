package com.adolfoeloy.taxtracker.product

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CertificateTest {

    @Test
    @DisplayName("should normalize 13-character certificate to 20-character format")
    fun shouldNormalize13CharacterCertificateTo20CharacterFormat() {
        // Given
        val shortCertificate = "1260023458960" // 13 characters
        val expectedLongCertificate = "12602370000023458960" // 20 characters

        // When
        val certificate = Certificate.createNormalizedCertificate(shortCertificate)

        // Then
        assertThat(certificate.value).isEqualTo(expectedLongCertificate)
    }

    @Test
    @DisplayName("should handle both short and long certificate formats")
    fun shouldHandleBothShortAndLongCertificateFormatsForSameProduct() {
        // Given
        val shortCertificate = "1260023458960"
        val expectedLongCertificate = "12602370000023458960"

        // When
        val certificate = Certificate.createNormalizedCertificate(shortCertificate)

        // Then
        assertThat(certificate.value).isEqualTo(expectedLongCertificate)
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
            val certificate = Certificate.createNormalizedCertificate(shortCert)

            // Then
            assertThat(certificate.value)
                .withFailMessage("Short certificate $shortCert should normalize to $expectedLongCert")
                .isEqualTo(expectedLongCert)
        }
    }

    @Test
    @DisplayName("should keep 20-character certificates unchanged")
    fun shouldKeep20CharacterCertificatesUnchanged() {
        // Given
        val longCertificate = "12602370000023458960"

        // When
        val certificate = Certificate.createNormalizedCertificate(longCertificate)

        // Then
        assertThat(certificate.value).isEqualTo(longCertificate)
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

        testCases.forEach { certificateNumber ->
            // When
            val certificate = Certificate.createNormalizedCertificate(
                certificateNumber,
            )

            // Then
            assertThat(certificate.value)
                .withFailMessage("Certificate $certificateNumber with length ${certificateNumber.length} should remain unchanged")
                .isEqualTo(certificateNumber)
        }
    }

    @Test
    @DisplayName("should create certificate with different number format")
    fun shouldCreateProductWithDifferentCertificateNumberFormat() {
        // Given
        val longCertificate = "12602370000023459123"

        // When
        val certificate = Certificate.createNormalizedCertificate( longCertificate)

        // Then
        assertThat(certificate.value).isEqualTo(longCertificate)
    }
}