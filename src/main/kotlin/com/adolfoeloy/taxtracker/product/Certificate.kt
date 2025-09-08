package com.adolfoeloy.taxtracker.product

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * Represents a certificate with normalization logic for different formats.
 */
@Embeddable
class Certificate{
    @Column(name = "certificate")
    var value: String = ""

    companion object {
        /**
         * Normalizes the certificate string and returns a Certificate instance.
         * If the certificate is 13 characters, expands it to 20 characters as per business rules.
         * If already 20 characters, returns as is.
         * Otherwise, returns the input as is.
         */
        fun createNormalizedCertificate(raw: String): Certificate {
            val normalized = when (raw.length) {
                13 -> {
                    val prefix = raw.take(3)
                    val suffix = raw.substring(5)
                    prefix + "023700000" + suffix
                }
                20 -> raw
                else -> raw
            }
            return Certificate().apply { value = normalized }
        }
    }
}
