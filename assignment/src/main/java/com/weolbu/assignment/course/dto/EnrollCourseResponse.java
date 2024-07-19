package com.weolbu.assignment.course.dto;

import java.util.List;

public record EnrollCourseResponse (
        boolean isSuccess,
        List<EnrollmentResult> detail
){

}
