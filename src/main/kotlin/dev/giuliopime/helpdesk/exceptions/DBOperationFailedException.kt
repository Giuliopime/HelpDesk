package dev.giuliopime.helpdesk.exceptions

class DBOperationFailedException(
    override val message: String,
    exception: Exception? = Exception("No exception info")
) : Exception(message, exception)
