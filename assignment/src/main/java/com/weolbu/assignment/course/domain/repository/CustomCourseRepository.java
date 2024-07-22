package com.weolbu.assignment.course.domain.repository;

import com.weolbu.assignment.course.domain.CourseOrder;
import com.weolbu.assignment.course.dto.CourseInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomCourseRepository {

    Page<CourseInfoDto> findUnenrolledCourses(Long memberId, CourseOrder courseOrder, Pageable pageable);
    Page<CourseInfoDto> findEnrolledCourses(Long memberId, CourseOrder courseOrder, Pageable pageable);
    Page<CourseInfoDto> findManagingCourses(Long memberId, CourseOrder courseOrder, Pageable pageable);

    Page<CourseInfoDto> findAllCourses(CourseOrder order, Pageable pageable);
}
