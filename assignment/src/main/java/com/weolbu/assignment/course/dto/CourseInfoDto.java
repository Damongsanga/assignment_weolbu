package com.weolbu.assignment.course.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CourseInfoDto {
    private Long courseId;
    private String title;
    private int maxStudents;
    private int currentStudents;
    private int price;
    private String instructorName;
}
