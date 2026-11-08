package com.adolfoeloy.taxtracker.vgbl.v2

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CsvUploadTest {

    private val csv = "TP_FUNDO_CLASSE;CNPJ_FUNDO_CLASSE\nCLASSES - FIF;26.756.416/0001-28\n"

    @Test
    fun `should read a plain CSV as is`() {
        val upload = MockMultipartFile("file", "inf_diario_fi_202508.csv", "text/csv", csv.toByteArray())

        assertThat(upload.csvInputStream().readAllBytes().decodeToString()).isEqualTo(csv)
    }

    @Test
    fun `should read the CSV inside a zip`() {
        val upload = MockMultipartFile("file", "inf_diario_fi_202508.zip", "application/zip", zip("inf_diario_fi_202508.csv" to csv))

        assertThat(upload.csvInputStream().readAllBytes().decodeToString()).isEqualTo(csv)
    }

    @Test
    fun `should recognise a zip by its content, not its filename`() {
        val upload = MockMultipartFile("file", "inf_diario_fi_202508.csv", "text/csv", zip("inf.csv" to csv))

        assertThat(upload.csvInputStream().readAllBytes().decodeToString()).isEqualTo(csv)
    }

    @Test
    fun `should skip directory entries inside a zip`() {
        val upload = MockMultipartFile("file", "x.zip", "application/zip", zip("data/" to "", "data/inf.csv" to csv))

        assertThat(upload.csvInputStream().readAllBytes().decodeToString()).isEqualTo(csv)
    }

    @Test
    fun `should reject a zip without files`() {
        val upload = MockMultipartFile("file", "x.zip", "application/zip", zip())

        assertThatThrownBy { upload.csvInputStream() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("The zip file does not contain any file")
    }

    @Test
    fun `should accept an empty upload without failing on the signature check`() {
        val upload = MockMultipartFile("file", "x.csv", "text/csv", ByteArray(0))

        assertThat(upload.csvInputStream().readAllBytes()).isEmpty()
    }

    private fun zip(vararg entries: Pair<String, String>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { out ->
            entries.forEach { (name, content) ->
                out.putNextEntry(ZipEntry(name))
                out.write(content.toByteArray())
                out.closeEntry()
            }
        }
        return bytes.toByteArray()
    }
}
