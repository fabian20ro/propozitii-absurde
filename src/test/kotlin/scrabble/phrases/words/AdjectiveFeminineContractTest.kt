package scrabble.phrases.words

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AdjectiveFeminineContractTest {

    @Test
    fun checkFeminineTransitions() {
        val cases = mapOf(
            "negru" to "neagră",
            "roșu" to "roșie",
            "sec" to "seacă",
            "des" to "deasă",
            "drept" to "dreaptă",
            "întreg" to "întreagă",
            "deșert" to "deșeartă",
            "mort" to "moartă",
            "calos" to "caloasă",
            "muncitor" to "muncitoare",
            "superior" to "superioară",
            "inferior" to "inferioară",
            "frumos" to "frumoasă",
            "zglobiu" to "zglobie",
            "stângaci" to "stângace",
            "integru" to "integră",
            "acru" to "acră",
            "ușurel" to "ușurică",
            "frumușel" to "frumușică",
            "micuțel" to "micuțică",
            "fidel" to "fidelă",
            "alb" to "albă",
            "verde" to "verde",
            "maro" to "maro",
            "gri" to "gri",

            // Pattern: endsWith("esc") -> dropLast(2) + "ască"
            "sălbatic" to "sălbatică",  // else branch (not esc): word+"ă"

            // Pattern: endsWith("eț") -> dropLast(1) + "ață"
            "măreț" to "măreață",

            // Pattern: endsWith("tor"/"șor"/"ior") -> dropLast(2) + "oară"
            "prietenos" to "prietenoasă",  // actually hits os rule; tor/șor/ior covered in AdjectiveTest

            // Pattern: endsWith("ru") (not hardcoded exceptions) -> dropLast(1) + "ă"
            "crud" to "crudă",

            // Pattern: else -> word + "ă" for consonant ending not covered above
            "pingin" to "pingină",
        )

        cases.forEach { (masculine, expected) ->
            val adj = Adjective(masculine)
            assertThat(adj.feminine).isEqualTo(expected)
        }
    }

    @Test
    fun shouldThrowOnBlankInput() {
        assertThrows<IllegalArgumentException> {
            Adjective("")
        }
        assertThrows<IllegalArgumentException> {
            Adjective("   ")
        }
    }
}
