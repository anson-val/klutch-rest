package com.example.model

import org.jetbrains.exposed.sql.Table

object Users: Table() {
    val id = integer("UserId").autoIncrement()
    val username = varchar("Username", 255).uniqueIndex()
    val password = varchar("Password", 255)
    val firstName = varchar("FirstName", 255)
    val lastName = varchar("LastName", 255)
    val email = varchar("Email", 255).uniqueIndex()
    val authority = integer("Authority")
    override val primaryKey = PrimaryKey(id, name = "PK_Users_Id")
}