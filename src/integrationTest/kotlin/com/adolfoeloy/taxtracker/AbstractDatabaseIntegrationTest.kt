package com.adolfoeloy.taxtracker

import com.adolfoeloy.taxtracker.config.DatabaseTestConfiguration
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(DatabaseTestConfiguration::class)
abstract class AbstractDatabaseIntegrationTest {
}