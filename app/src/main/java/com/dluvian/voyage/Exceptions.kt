package com.dluvian.voyage

sealed class VoyageException : Throwable()

class AlreadyUnfollowedException : VoyageException()
class AlreadyFollowedException : VoyageException()
class FailedToSignException : VoyageException()
class InvalidSecretException : VoyageException()
