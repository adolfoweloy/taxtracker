package com.adolfoeloy.taxtracker.product

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDate

@Controller
@RequestMapping("/settings/products")
class ProductPageController(
    private val productRepository: ProductRepository
) {

    @GetMapping
    fun listProducts(model: Model): String {
        val today = LocalDate.now()
        val (matured, active) = productRepository.findAll().partition { it.matureAt.isBefore(today) }
        model.addAttribute("activeProducts", active)
        model.addAttribute("maturedProducts", matured)
        return "products"
    }
}
