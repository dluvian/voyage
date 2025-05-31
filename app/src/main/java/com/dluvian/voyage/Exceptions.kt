package com.dluvian.voyage

sealed class VoyageException : Throwable()

class AlreadyUnfollowedException : VoyageException()
class EventNotInDatabaseException : VoyageException()
class AlreadyFollowedException : VoyageException()
