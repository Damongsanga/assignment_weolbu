package com.weolbu.assignment.course.application;

import com.weolbu.assignment.auth.application.AuthService;
import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.repository.CourseRepository;
import com.weolbu.assignment.course.dto.CourseCreateRequest;
import com.weolbu.assignment.course.dto.CourseEnrollRequest;
import com.weolbu.assignment.course.dto.EnrollCourseResponse;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.global.exception.errorcode.CustomErrorCode;
import com.weolbu.assignment.member.application.MemberService;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.MemberType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseEnrollmentProcessor courseEnrollmentProcessor;
    @Mock
    private AuthService authService;
    @Mock
    private MemberService memberService;
    @Mock
    private CourseRepository courseRepository;


    @Test
    void 강사는_강의를_만들_수_있다(){
        Member managingInstructor = mock(Member.class);
        when(managingInstructor.getType()).thenReturn(MemberType.INSTRUCTOR);

        Course course = new Course("test_강의1", 20000, 0, 30, managingInstructor);

        given(authService.findMemberId()).willReturn(1L);
        given(memberService.findById(1L)).willReturn(managingInstructor);
        given(courseRepository.save(any(Course.class))).willReturn(course);

        CourseCreateRequest request = new CourseCreateRequest("test_강의1", 30, 20000);

        // when
        courseService.createCourse(request);

        // then
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void 사용자는_여러개의_강의에_수강신청할_수_있다(){
        Member student = mock(Member.class);

        CourseEnrollRequest request = new CourseEnrollRequest(List.of(1L, 2L, 3L));

        given(authService.findMemberId()).willReturn(1L);
        given(memberService.findById(1L)).willReturn(student);
        willDoNothing().given(courseEnrollmentProcessor).processEnrollment(1L, student);
        willDoNothing().given(courseEnrollmentProcessor).processEnrollment(2L, student);
        willDoNothing().given(courseEnrollmentProcessor).processEnrollment(3L, student);

        EnrollCourseResponse response = courseService.enrollCourse(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.detail().get(0).isEnrolled()).isTrue();
        assertThat(response.detail().get(0).courseId()).isEqualTo(1L);
        assertThat(response.detail().get(1).isEnrolled()).isTrue();
        assertThat(response.detail().get(1).courseId()).isEqualTo(2L);
        assertThat(response.detail().get(2).isEnrolled()).isTrue();
        assertThat(response.detail().get(2).courseId()).isEqualTo(3L);
    }

    @Test
    void 사용자는_일부_수강신청에_실패해도_나머지_강의_수강신청은_성공할_수_있다(){
        Member student = mock(Member.class);

        CourseEnrollRequest request = new CourseEnrollRequest(List.of(1L, 2L, 3L));

        BaseException exception = new BaseException(CustomErrorCode.FULL_COURSE);

        given(authService.findMemberId()).willReturn(1L);
        given(memberService.findById(1L)).willReturn(student);
        willThrow(exception).given(courseEnrollmentProcessor).processEnrollment(1L, student);
        willDoNothing().given(courseEnrollmentProcessor).processEnrollment(2L, student);
        willThrow(exception).given(courseEnrollmentProcessor).processEnrollment(3L, student);

        EnrollCourseResponse response = courseService.enrollCourse(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.detail().get(0).isEnrolled()).isFalse();
        assertThat(response.detail().get(0).courseId()).isEqualTo(1L);
        assertThat(response.detail().get(1).isEnrolled()).isTrue();
        assertThat(response.detail().get(1).courseId()).isEqualTo(2L);
        assertThat(response.detail().get(2).isEnrolled()).isFalse();
        assertThat(response.detail().get(2).courseId()).isEqualTo(3L);
    }


}