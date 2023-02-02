package com.example.model

import org.jetbrains.exposed.sql.Table

object Problems: Table() {
    val id = integer("ProblemId").autoIncrement()
    val title = text("Title")
    val description = text("Description")
    override val primaryKey = PrimaryKey(id, name = "PK_Problems_Id")
}