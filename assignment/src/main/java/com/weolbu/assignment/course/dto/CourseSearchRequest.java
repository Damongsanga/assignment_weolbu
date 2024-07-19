package com.weolbu.assignment.course.dto;

import com.weolbu.assignment.course.domain.CourseFetchType;
import com.weolbu.assignment.course.domain.CourseOrder;
import jakarta.validation.constraints.NotNull;

public record CourseSearchRequest (
        @NotNull CourseFetchType type,
        @NotNull CourseOrder order
){
}
