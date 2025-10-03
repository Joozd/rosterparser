package nl.joozd.rosterparser.utils

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.sample
import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.progress.Progress
import java.io.InputStream
import kotlin.time.Duration

@FlowPreview // because _stateFlow.sample is marked FlowPreview
class SingleUseRosterParserWithFlowFactory(sampleRate: Duration) {
    private var consumed = false

    private val _stateFlow = MutableStateFlow<Progress>(Progress.NOT_STARTED)
    private val progressFlow = _stateFlow.sample(sampleRate)

    val wrappedOnProgress: (Progress) -> Unit = { progress ->
        _stateFlow.value = progress
    }

    /**
     * Builds a RosterParser.
     * Progress on generating the parser can be observed through [progressFlow]
     * Progress from the parser can also be observed through [progressFlow]
     * @see RosterParser.ofInputStream
     */
    @Synchronized
    fun build(inputStream: InputStream, mimeType: String): RosterParser {
        require(!consumed)
        consumed = true
        return RosterParser.ofInputStream(inputStream, mimeType, wrappedOnProgress)
    }
}