package com.adolfoeloy.taxtracker.forex

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/settings/exchange-rates")
class ExchangeRatePageController(
    private val exchangeRateRepository: ExchangeRateRepository
) {

    @GetMapping
    fun listRates(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) date: String?
    ): String {
        val pageable = PageRequest.of(page, size)

        val rates = if (!date.isNullOrBlank()) {
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            model.addAttribute("date", date)
            exchangeRateRepository.findByRateAtOrderBySourceAsc(localDate, pageable)
        } else {
            exchangeRateRepository.findAllByOrderByRateAtDesc(pageable)
        }

        model.addAttribute("rates", rates)
        return "exchange_rates"
    }
}
