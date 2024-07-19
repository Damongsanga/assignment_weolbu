package com.weolbu.assignment.course.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CourseEnrollRequest (
        @NotNull List<Long> enroll
){
}
