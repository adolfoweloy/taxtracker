package com.adolfoeloy.taxtracker.transaction

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int> {
    // Additional query methods can be defined here if needed
}