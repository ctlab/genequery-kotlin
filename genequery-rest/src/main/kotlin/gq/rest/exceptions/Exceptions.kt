package gq.rest.exceptions

class BadRequestException : Exception {
    constructor(t: Throwable) : super(t)
    constructor(message: String) : super(message)
}