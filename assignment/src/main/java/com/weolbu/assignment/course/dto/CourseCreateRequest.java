package com.weolbu.assignment.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CourseCreateRequest (
        @NotBlank
        String title,
        @Positive
        int maxStudents,
        @PositiveOrZero
        int price
){
}
