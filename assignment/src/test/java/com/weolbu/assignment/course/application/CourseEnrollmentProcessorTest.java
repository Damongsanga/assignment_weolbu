package com.weolbu.assignment.course.application;

import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.repository.CourseMemberRepository;
import com.weolbu.assignment.course.domain.repository.CourseRepository;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.global.exception.errorcode.CustomErrorCode;
import com.weolbu.assignment.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseEnrollmentProcessorTest {

    @InjectMocks
    private CourseEnrollmentProcessor courseEnrollmentProcessor;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMemberRepository courseMemberRepository;

    @Nested
    @DisplayName("수강 신청")
    class Enroll {

        @Test
        void 사용자는_학생_강사_상관없이_단일_강의에_수강신청할_수_있다(){
            // given
            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);

            Member managingInstructor = mock(Member.class);
            when(member.getId()).thenReturn(2L);

            Course course = new Course("강의1", 20000, 0, 30, managingInstructor);

            given(courseRepository.findByIdForUpdate(1L)).willReturn(Optional.of(course));
            given(courseMemberRepository.existsByCourseAndMember(course, member)).willReturn(false);

            // when
            courseEnrollmentProcessor.processEnrollment(1L, member);

            // then
            assertThat(course.getCurrentStudents()).isEqualTo(1);

        }


        @Test
        void 이미_신청한_강의는_수강신청할_수_없다(){

            // given
            Member member = mock(Member.class);
            Member managingInstructor = mock(Member.class);
            Course course = new Course("강의1", 20000, 0, 30, managingInstructor);

            given(courseRepository.findByIdForUpdate(1L)).willReturn(Optional.of(course));
            given(courseMemberRepository.existsByCourseAndMember(course, member)).willReturn(true);

            // when / then
            assertThatThrownBy(() -> courseEnrollmentProcessor.processEnrollment(1L, member))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_ENROLLED.getMessage());

        }

    }



}