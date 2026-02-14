package com.adolfoeloy.taxtracker.vgbl

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/settings/vgbl-funds")
class VGBLFundPageController(
    private val vgblFundRepository: VGBLFundRepository,
    private val vgblFundService: VGBLFundService
) {

    @GetMapping
    fun listFunds(model: Model): String {
        model.addAttribute("funds", vgblFundRepository.findAll())
        return "vgbl_funds"
    }

    @PostMapping
    fun addFund(
        @RequestParam cnpj: String,
        @RequestParam planName: String,
        @RequestParam fundName: String,
        @RequestParam quotas: String,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            vgblFundService.saveFund(VGBLFundRequest(cnpj, planName, fundName, quotas))
            redirectAttributes.addFlashAttribute("successMessage", "Fund '$fundName' added successfully")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add fund: ${e.message}")
        }
        return "redirect:/settings/vgbl-funds"
    }
}
