package com.weolbu.assignment.course.presentation;

import com.weolbu.assignment.course.application.CourseService;
import com.weolbu.assignment.course.domain.CourseFetchType;
import com.weolbu.assignment.course.domain.CourseOrder;
import com.weolbu.assignment.course.dto.CourseCreateRequest;
import com.weolbu.assignment.course.dto.CourseEnrollRequest;
import com.weolbu.assignment.course.dto.CourseInfoDto;
import com.weolbu.assignment.course.dto.EnrollCourseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping()
    public ResponseEntity<Long> createCourse(
            @Valid @RequestBody CourseCreateRequest request
    ){
        return ResponseEntity.status(CREATED).body(courseService.createCourse(request));
    }

    @GetMapping()
    public ResponseEntity<Page<CourseInfoDto>> getCourses(
            @RequestParam(defaultValue = "UNENROLLED") CourseFetchType type,
            @RequestParam(defaultValue = "LATEST") CourseOrder order,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ){
        return ResponseEntity.ok(courseService.getCourses(type, order, pageable));
    }

    @PostMapping("/enroll")
    public ResponseEntity<EnrollCourseResponse> enroll(
            @Valid @RequestBody CourseEnrollRequest request
    ){
        return ResponseEntity.ok(courseService.enrollCourse(request));
    }

}
