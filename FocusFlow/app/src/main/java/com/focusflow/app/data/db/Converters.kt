package com.focusflow.app.data.db

import androidx.room.TypeConverter
import com.focusflow.app.domain.model.Priority
import com.focusflow.app.domain.model.TaskCategory
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter fun fromLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter fun fromLocalDateTime(dt: LocalDateTime?): String? = dt?.toString()
    @TypeConverter fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter fun fromPriority(p: Priority): String = p.name
    @TypeConverter fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter fun fromCategory(c: TaskCategory): String = c.name
    @TypeConverter fun toCategory(value: String): TaskCategory = TaskCategory.valueOf(value)
}
