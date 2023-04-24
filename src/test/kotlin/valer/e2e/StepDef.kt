package valer.e2e

import io.cucumber.java.ru.Когда
import io.cucumber.java.ru.Тогда
import io.ktor.http.*
import io.ktor.server.engine.*
import io.mockk.*
import org.junit.jupiter.api.assertThrows
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import valer.main
import kotlin.test.assertEquals


class StepDef {

    lateinit var envs: EnvironmentVariables

    @Когда("^пользователь не указал переменную (.+)$")
    fun пользовательНеУказалПеременную(env: String) {
        when (env) {
            "PORT" -> envs = EnvironmentVariables("NEIGHBORS", "1")
            "NEIGHBORS" -> envs = EnvironmentVariables("PORT", ",")
            "MASTER" -> {
                envs = EnvironmentVariables("PORT", ",")
                envs.set("NEIGHBORS", "1")
            }

            else -> {
                envs = EnvironmentVariables("PORT", ",")
                envs.set("NEIGHBORS", "1")
                envs.set("MASTER", "false")
            }
        }
    }

    @Тогда("^пользователь получит ошибку (\\w+) .*$")
    fun пользовательПолучитОшибку(env: String) {
        envs.execute {
            val exception = assertThrows<NullPointerException> { main() }
            assertEquals("getenv(\"$env\") must not be null", exception.message)
        }
    }

    @Тогда("^узел будет ждать блок из сети$")
    fun узелБудетЖдатьБлок() {
        envs.execute {
            assertThrows<NumberFormatException> { main() }
        }
    }

    @Когда("^пользователь указал переменную (.+)$")
    fun пользовательУказалПеременную(env: String) {
        when (env) {
            "MASTER=true" -> {
                envs = EnvironmentVariables("PORT", ",")
                envs.set("NEIGHBORS", "1")
                envs.set("MASTER", "true")
            }
        }
    }

    @Тогда("^узел начнет создавать блоки$")
    fun узелНачнетСоздаватьБлоки() {
        envs.execute {
            assertThrows<NumberFormatException> { main() }
        }
    }
}

