package nl.joozd.rosterparser.testing

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedSimulatorDuty
import nl.joozd.rosterparser.Person
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Sample duties for use in client testing.
 * These duties can be provided in a roster by calling [buildSampleRoster]
 */
object SampleDuties {
    // Alle velden ingevuld, als PIC overdag op een VFR vlucht
    val picDayVfr = ParsedFlight(
        date = LocalDate.of(2024, 6, 14),
        flightNumber = "KL1234",
        takeoffAirport = "AMS",
        landingAirport = "JFK",
        departureTime = LocalDateTime.of(2024, 6, 14, 10, 30),
        arrivalTime = LocalDateTime.of(2024, 6, 14, 13, 45),
        overriddenTotalTime = 7.hours,
        multiPilotTime = 5.hours,
        nightTime = Duration.ZERO,
        ifrTime = Duration.ZERO,
        picTime = 7.hours,
        picusTime = Duration.ZERO,
        xcTime = 4.hours,
        actualIfrTime = Duration.ZERO,
        dualReceivedTime = Duration.ZERO,
        dualGivenTime = Duration.ZERO,
        aircraftType = "Boeing 777",
        aircraftRegistration = "PH-BQA",
        numberOfTakeoffsByDay = 1,
        numberOfTakeoffsByNight = 0,
        numberOfLandingsByDay = 1,
        numberOfLandingsByNight = 0,
        numberOfAutolands = 0,
        crewSize = 2,
        atControlsForTakeoff = true,
        atControlsForLanding = true,
        augmentedCrewTimeForTakeoffLanding = Duration.ZERO,
        augmentedCrewFixedRestTime = Duration.ZERO,
        pilotInCommand = Person("John", "Doe", "A", "Mr.", "Captain", "123456"),
        personsNotPIC = listOf(Person("Jane", "Smith", "B", "Ms.", "First Officer", "654321")),
        isPF = true,
        isPICDuty = true,
        isPICUSDuty = false,
        isCopilotDuty = false,
        isInstructorDuty = false,
        isDualDuty = false,
        remarks = "Smooth VFR flight",
        signatureSVG = "<svg>...</svg>",
        isDeadHeading = false
    )

    // Alle velden ingevuld, als copilot 's nachts op een IFR vlucht
    val copilotNightIfr = ParsedFlight(
        date = LocalDate.of(2024, 6, 15),
        flightNumber = "AF5678",
        takeoffAirport = "CDG",
        landingAirport = "LHR",
        departureTime = LocalDateTime.of(2024, 6, 15, 22, 0),
        arrivalTime = LocalDateTime.of(2024, 6, 15, 23, 0),
        overriddenTotalTime = 1.hours,
        multiPilotTime = 1.hours,
        nightTime = 1.hours,
        ifrTime = 1.hours,
        picTime = Duration.ZERO,
        picusTime = Duration.ZERO,
        xcTime = 45.minutes,
        actualIfrTime = 30.minutes,
        dualReceivedTime = Duration.ZERO,
        dualGivenTime = Duration.ZERO,
        aircraftType = "Airbus A320",
        aircraftRegistration = "F-GKXA",
        numberOfTakeoffsByDay = 0,
        numberOfTakeoffsByNight = 1,
        numberOfLandingsByDay = 0,
        numberOfLandingsByNight = 1,
        numberOfAutolands = 0,
        crewSize = 3,
        atControlsForTakeoff = true,
        atControlsForLanding = false,
        augmentedCrewTimeForTakeoffLanding = Duration.ZERO,
        augmentedCrewFixedRestTime = 20.minutes,
        pilotInCommand = Person("Alice", "Johnson", "C", "Ms.", "Captain", "789012"),
        personsNotPIC = listOf(Person("Bob", "Brown", "D", "Mr.", "First Officer", "210987")),
        isPF = false,
        isPICDuty = false,
        isPICUSDuty = true,
        isCopilotDuty = true,
        isInstructorDuty = false,
        isDualDuty = false,
        remarks = "Night IFR flight with some turbulence",
        signatureSVG = "<svg>...</svg>",
        isDeadHeading = false
    )

    // Als instructeur en PIC
    val picInstructor = ParsedFlight(
        date = LocalDate.of(2024, 6, 16),
        flightNumber = "LH3456",
        takeoffAirport = "FRA",
        landingAirport = "ORD",
        departureTime = LocalDateTime.of(2024, 6, 16, 8, 45),
        arrivalTime = LocalDateTime.of(2024, 6, 16, 11, 15),
        overriddenTotalTime = 9.hours,
        multiPilotTime = 7.hours,
        nightTime = 4.hours,
        ifrTime = 5.hours,
        picTime = 7.hours,
        picusTime = Duration.ZERO,
        xcTime = 6.hours,
        actualIfrTime = 5.hours,
        dualReceivedTime = Duration.ZERO,
        dualGivenTime = 1.hours,
        aircraftType = "Airbus A380",
        aircraftRegistration = "D-AIMA",
        numberOfTakeoffsByDay = 1,
        numberOfTakeoffsByNight = 0,
        numberOfLandingsByDay = 1,
        numberOfLandingsByNight = 0,
        numberOfAutolands = 0,
        crewSize = 4,
        atControlsForTakeoff = true,
        atControlsForLanding = true,
        augmentedCrewTimeForTakeoffLanding = Duration.ZERO,
        augmentedCrewFixedRestTime = 2.hours,
        pilotInCommand = Person("Charlie", "Davis", "E", "Mr.", "Captain", "345678"),
        personsNotPIC = listOf(Person("David", "Evans", "F", "Mr.", "First Officer", "876543")),
        isPF = true,
        isPICDuty = true,
        isPICUSDuty = false,
        isCopilotDuty = false,
        isInstructorDuty = true,
        isDualDuty = false,
        remarks = "Training flight with new copilot",
        signatureSVG = "<svg>...</svg>",
        isDeadHeading = false
    )

    // Met zoveel mogelijk velden null en alle strings en lijsten leeg, en alle nummers/duur op nul indien ze niet null kunnen zijn
    val nullsAndZeroes = ParsedFlight(
        date = LocalDate.of(2024, 6, 17),
        takeoffAirport = "",
        landingAirport = "",
        departureTime = LocalDateTime.of(2024, 6, 17, 0, 0),
        arrivalTime = LocalDateTime.of(2024, 6, 17, 0, 0),
        overriddenTotalTime = Duration.ZERO,
        multiPilotTime = Duration.ZERO,
        nightTime = Duration.ZERO,
        ifrTime = Duration.ZERO,
        picTime = Duration.ZERO,
        picusTime = Duration.ZERO,
        xcTime = Duration.ZERO,
        actualIfrTime = Duration.ZERO,
        dualReceivedTime = Duration.ZERO,
        dualGivenTime = Duration.ZERO,
        aircraftType = "",
        aircraftRegistration = "",
        numberOfTakeoffsByDay = 0,
        numberOfTakeoffsByNight = 0,
        numberOfLandingsByDay = 0,
        numberOfLandingsByNight = 0,
        numberOfAutolands = 0,
        crewSize = 0,
        atControlsForTakeoff = null,
        atControlsForLanding = null,
        augmentedCrewTimeForTakeoffLanding = Duration.ZERO,
        augmentedCrewFixedRestTime = Duration.ZERO,
        pilotInCommand = null,
        personsNotPIC = emptyList(),
        isPF = null,
        isPICDuty = null,
        isPICUSDuty = null,
        isCopilotDuty = null,
        isInstructorDuty = null,
        isDualDuty = null,
        remarks = "",
        signatureSVG = "",
        isDeadHeading = null
    )

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Simulator duty met alle velden ingevuld
    val simAllFieldsFilled = ParsedSimulatorDuty(
        date = LocalDate.of(2024, 6, 14),
        duration = 4.hours,
        simulatorType = "B789",
        remarks = "Standard training session",
        persons = listOf(
            Person("John", "Doe", "A", "Mr.", "Captain", "123456"),
            Person("Jane", "Smith", "B", "Ms.", "First Officer", "654321")
        ),
        instructionGiven = false
    )

    // Simulator duty met zoveel mogelijk velden null en alle strings en lijsten leeg, en alle nummers/duur op nul indien ze niet null kunnen zijn
    val simmNullsAndZeroes = ParsedSimulatorDuty(
        date = LocalDate.of(2024, 6, 15),
        duration = Duration.ZERO,
        simulatorType = "",
        remarks = "",
        persons = emptyList(),
        instructionGiven = null
    )

    // Simulator duty als instructeur
    val simAsInstructor = ParsedSimulatorDuty(
        date = LocalDate.of(2024, 6, 16),
        duration = 3.5.hours,
        simulatorType = "FNPT-2",
        remarks = "Instructor session",
        persons = listOf(
            Person("Alice", "Johnson", "C", "Ms.", "Captain", "789012"),
            Person("Bob", "Brown", "D", "Mr.", "First Officer", "210987")
        ),
        instructionGiven = true
    )

    val flightDuties = listOf(
        SampleDuties.picDayVfr,
        SampleDuties.copilotNightIfr,
        SampleDuties.picInstructor,
        SampleDuties.nullsAndZeroes
    )

    val simDuties = listOf(
        simAsInstructor,
        simAllFieldsFilled,
        simmNullsAndZeroes
    )


}