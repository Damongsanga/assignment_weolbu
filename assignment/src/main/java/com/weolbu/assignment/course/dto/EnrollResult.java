package com.weolbu.assignment.course.dto;

public record EnrollResult(
        long courseId,
        boolean isEnrolled,
        String message
){
}
