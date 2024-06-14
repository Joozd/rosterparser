import nl.joozd.rosterparser.ParsedRoster
import testutils.makeDummyFlightDuty
import testutils.makeDummySimDuty
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ParsedRosterTest {


    /**
     * Test that the period calculation defaults to LocalDate.MIN..LocalDate.MIN
     * when no duties have been added to the builder. This case represents an empty or undefined roster period.
     */
    @Test
    fun `test period calculation with no duties`() {
        val parsedRoster =  ParsedRoster.build {
            // do nothing
        }
        val expectedPeriod = LocalDate.MIN..LocalDate.MIN
        assertEquals(expectedPeriod, parsedRoster.coveredDates, "Period should be MIN..MIN when no duties are added")
    }

    /**
     * Test the period calculation with duties spanning multiple days. This test ensures that the calculated
     * period accurately reflects the range from the earliest duty date to the latest duty date, covering
     * the entire span of duty dates added to the builder.
     */
    @Test
    fun `test period calculation with duties spanning multiple days`() {
        val parsedRoster =  ParsedRoster.build {
            // Adding two duties with different dates to span multiple days
            addDuty(makeDummyFlightDuty(LocalDate.of(2022, 3, 5)))
            addDuty(makeDummySimDuty(LocalDate.of(2022, 3, 7)))
        }
        val expectedPeriod = LocalDate.of(2022, 3, 5)..LocalDate.of(2022, 3, 7)
        assertEquals(expectedPeriod, parsedRoster.coveredDates, "Period should span from the first to the last duty date")
    }

    /**
     * Test if entering a fixed period will overrule period calculation.
     */
    @Test
    fun `test period calculation does not override fixed entry`() {
        val fixedPeriod = LocalDate.of(2022, 3, 5)..LocalDate.of(2022, 3, 7)
        val parsedRoster =  ParsedRoster.build {
            // Adding two duties with different dates to span multiple days
            addDuty(makeDummyFlightDuty(LocalDate.of(2022, 3, 5)))
            addDuty(makeDummySimDuty(LocalDate.of(2022, 3, 7)))
            rosterPeriod = fixedPeriod
        }

        assertEquals(fixedPeriod, parsedRoster.coveredDates, "Period should span from the first to the last duty date")
    }

    /**
     * Test the period calculation with all duties occurring on a single day. This scenario checks that
     * the builder correctly identifies when all duties fall within the same day and sets the roster period
     * to just that day, ensuring the period does not incorrectly span additional days.
     */
    @Test
    fun `test period calculation with duties on a single day`() {
        val parsedRoster =  ParsedRoster.build {
            // Adding two duties on the same day
            addDuty(makeDummyFlightDuty(LocalDate.of(2022, 4, 1)))
            addDuty(makeDummySimDuty(LocalDate.of(2022, 4, 1)))
        }
        val expectedPeriod = LocalDate.of(2022, 4, 1)..LocalDate.of(2022, 4, 1)
        assertEquals(expectedPeriod, parsedRoster.coveredDates, "Period should span from the first to the last duty date")
    }
}