package com.weolbu.assignment.course.application;

import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.repository.CourseMemberRepository;
import com.weolbu.assignment.course.domain.repository.CourseRepository;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.ALREADY_ENROLLED;
import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.NO_COURSE;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentProcessor {

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEnrollment(Long courseId, Member member) {
        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new BaseException(NO_COURSE));
        if (courseMemberRepository.existsByCourseAndMember(course, member)) throw new BaseException(ALREADY_ENROLLED);
        course.enroll(member);
    }
}
