package nl.joozd.rosterparser.parsers.progress

/**
 * The progress of a parsing action
 */
sealed interface Progress{
    /**
     * This action has not yet been created or started. Initial value.
     */
    object NOT_STARTED: Progress
    /**
     * This action has been created, but has not been started yet
     */
    object CREATED: Progress

    /**
     * This action has been started, but no progress has been reported (yet)
     */
    object STARTED: Progress

    /**
     * This action has finished.
     */
    object FINISHED: Progress

    /**
     * Failed to complete this action.
     */
    object FAILED: Progress
}

/**
 * Reading the file. [fractionOfWorkDone] has been read.
 */
class ReadingFile(val fractionOfWorkDone: Float): Progress

/**
 * This action is in progress. [fractionOfWorkDone] has been done.
 */
class Parsing(val fractionOfWorkDone: Float): Progress