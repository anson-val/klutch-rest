package com.example.classes.exceptions

class IdAlreadyExistsException(id: String, message: String = "Problem ID $id already exists"): Exception(message)