package com.weolbu.assignment.course.dto;

public record EnrollmentResult(
        long courseId,
        boolean isEnrolled,
        String message
){
}
