package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.properties.TaxProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(TaxProperties::class)
class TaxtrackerApplication

fun main(args: Array<String>) {
	runApplication<TaxtrackerApplication>(*args)
}
