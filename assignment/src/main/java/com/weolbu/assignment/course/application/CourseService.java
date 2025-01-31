package com.weolbu.assignment.course.application;

import com.weolbu.assignment.auth.application.AuthService;
import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.CourseFetchType;
import com.weolbu.assignment.course.domain.CourseOrder;
import com.weolbu.assignment.course.domain.repository.CourseRepository;
import com.weolbu.assignment.course.dto.*;

import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.application.MemberService;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.MemberType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final AuthService authService;
    private final MemberService memberService;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentProcessor courseEnrollmentProcessor;

    @Transactional
    public Long createCourse(CourseCreateRequest request) {
        Long memberId = authService.findMemberId();
        Member member = memberService.findById(memberId);

        if (checkTitleExits(request.title()))
            throw new BaseException(DUPLICATED_TITLE);

        Course course = Course.of(request, member);
        return courseRepository.save(course).getId();
    }

    public boolean checkTitleExits(String title) {
        return courseRepository.existsByTitle(title);
    }

    public Page<CourseInfoDto> getCourses(CourseFetchType type, CourseOrder order, Pageable pageable) {
        Long memberId = authService.findMemberId();

        Page<CourseInfoDto> courseDtos = Page.empty();
        switch (type) {
            case ALL -> courseDtos = courseRepository.findAllCourses(order, pageable);
            case UNENROLLED -> courseDtos = courseRepository.findUnenrolledCourses(memberId, order, pageable);
            case ENROLLED -> courseDtos = courseRepository.findEnrolledCourses(memberId, order, pageable);
            case MANAGING -> {

                Member member = memberService.findById(memberId);
                if (!member.getType().equals(MemberType.INSTRUCTOR)) {
                    throw new BaseException(NOT_INSTRUCTOR);
                }

                courseDtos = courseRepository.findManagingCourses(memberId, order, pageable);
            }
        }

        return courseDtos;
    }

    @Transactional
    public EnrollCourseResponse enrollCourse(CourseEnrollRequest request) {
        Long memberId = authService.findMemberId();
        Member member = memberService.findById(memberId);
        List<EnrollResult> results = new ArrayList<>();
        boolean isSuccess = true;

        for(long courseId : request.enroll()){
            try{
                courseEnrollmentProcessor.processEnrollment(courseId, member);
                results.add(new EnrollResult(courseId, true, "성공적으로 수강 신청되었습니다."));
            } catch (BaseException e){
                results.add(new EnrollResult(courseId, false, e.getMessage()));
                isSuccess = false;
            }
        }

        return new EnrollCourseResponse(isSuccess, results);
    }
}
