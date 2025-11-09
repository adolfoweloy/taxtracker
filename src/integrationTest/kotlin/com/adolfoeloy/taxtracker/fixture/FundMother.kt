package com.adolfoeloy.taxtracker.fixture

import com.adolfoeloy.taxtracker.vgbl.VGBLFund
import java.math.BigDecimal

class FundMother {

    companion object {
        fun createFund(cnpjInput: String, quotasOwned: BigDecimal): VGBLFund {
            val fundExample = VGBLFund().apply {
                cnpj = cnpjInput
                planName = "Plano Exemplo"
                fundName = "Fundo Exemplo"
                quotas = quotasOwned
            }
            return fundExample
        }
    }

}