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
    public ResponseEntity<Void> createCourse(
            @Valid @RequestBody CourseCreateRequest request
    ){
        courseService.createCourse(request);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping()
    public ResponseEntity<Page<CourseInfoDto>> getCourses(
            @RequestParam(defaultValue = "UNSIGNED") CourseFetchType type,
            @NotNull @RequestParam CourseOrder order,
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
