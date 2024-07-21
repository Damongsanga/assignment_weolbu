package com.weolbu.assignment.course.domain.repository;

import com.weolbu.assignment.course.domain.CourseOrder;
import com.weolbu.assignment.course.dto.CourseInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomCourseRepository {

    Page<CourseInfoDto> findNotSignedCourses(Long memberId, CourseOrder courseOrder, Pageable pageable);
    Page<CourseInfoDto> findSignedCourse(Long memberId, CourseOrder courseOrder, Pageable pageable);
    Page<CourseInfoDto> findManagingCourse(Long memberId, CourseOrder courseOrder, Pageable pageable);
}
