package com.adolfoeloy.taxtracker.vgbl.v2

import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

// every zip signature (local file header, empty archive) starts with these two bytes
private val ZIP_MAGIC = "PK".toByteArray()

/**
 * Opens the uploaded CSV, unzipping it first when the upload is a zip archive. CVM publishes its
 * files zipped with a single CSV inside, so only the first file of the archive is read.
 *
 * A zip is recognised by its signature rather than its filename, so a renamed file still works.
 */
fun MultipartFile.csvInputStream(): InputStream {
    val input = BufferedInputStream(inputStream)
    input.mark(ZIP_MAGIC.size)
    val header = input.readNBytes(ZIP_MAGIC.size)
    input.reset()

    if (!header.contentEquals(ZIP_MAGIC)) return input

    val zip = ZipInputStream(input)
    generateSequence { zip.nextEntry }.firstOrNull { !it.isDirectory }
        ?: throw IllegalArgumentException("The zip file does not contain any file")

    return zip
}
