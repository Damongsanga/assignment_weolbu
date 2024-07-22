package com.weolbu.assignment.course.domain;

import com.weolbu.assignment.course.dto.CourseCreateRequest;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.MemberType;
import com.weolbu.assignment.member.dto.MemberJoinRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourseTest {

    @Nested
    @DisplayName("강의 생성")
    class Create {
        @Test
        void 강사는_새로운_강의를_만들_수_있다(){
            // given
            CourseCreateRequest request = new CourseCreateRequest("부동산 강의", 20, 30000);
            Member instructor = createInstructor();

            // when
            Course course = Course.of(request, instructor);

            // then
            assertThat(course.getInstructor().getName()).isEqualTo("강사1");
            assertThat(course.getTitle()).isEqualTo("부동산 강의");
            assertThat(course.getPrice()).isEqualTo(30000);
            assertThat(course.getMaxStudents()).isEqualTo(20);
            assertThat(course.getCurrentStudents()).isEqualTo(0);
        }

        @Test
        void 학생은_강의를_만들_수_없다(){
            // given
            CourseCreateRequest request = new CourseCreateRequest("부동산 강의", 20, 30000);
            Member student = createStudent();

            // when / then
            assertThatThrownBy(() -> Course.of(request, student))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(NOT_INSTRUCTOR.getMessage());
        }
    }


    @Nested
    @DisplayName("수강 신청")
    class Enroll {

        @Test
        void 학생과_강사는_수강신청을_할_수_있다(){
            // given
            Member instructor = createInstructorMock(1L);
            Member otherInstructor = createInstructorMock(2L);
            Member student = createStudentMock(3L);

            CourseCreateRequest request = new CourseCreateRequest("부동산 강의", 20, 30000);
            Course course = Course.of(request, instructor);

            // when
            course.enroll(student);
            course.enroll(otherInstructor);

            // then
            assertThat(course.getCurrentStudents()).isEqualTo(2);
        }

        private static Member createInstructorMock(Long id) {
            Member instructor = mock(Member.class);
            when(instructor.getId()).thenReturn(id);
            when(instructor.getType()).thenReturn(MemberType.INSTRUCTOR);
            return instructor;
        }

        private static Member createStudentMock(Long id) {
            Member instructor = mock(Member.class);
            when(instructor.getId()).thenReturn(id);
            when(instructor.getType()).thenReturn(MemberType.STUDENT);
            return instructor;
        }

        @Test
        void 담당강사는_자신의_강의에_수강신청할_수_없다(){
            // given
            Member instructor = createInstructor();
            CourseCreateRequest request = new CourseCreateRequest("부동산 강의", 20, 30000);
            Course course = Course.of(request, instructor);

            // when / then
            assertThatThrownBy(() -> course.enroll(instructor))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(SELF_ENROLLMENT.getMessage());
        }

        @Test
        void 수강인원이_가득차면_수강_신청할_수_없다(){
            // given
            Member instructor = createInstructorMock(1L);
            Member student = createStudentMock(2L);
            Member secondStudent = createStudentMock(3L);

            CourseCreateRequest request = new CourseCreateRequest("부동산 강의", 1, 30000);
            Course course = Course.of(request, instructor);
            course.enroll(student);

            // when / then
            assertThatThrownBy(() -> course.enroll(secondStudent))
                    .isInstanceOf(BaseException.class)
                    .hasMessageContaining(FULL_COURSE.getMessage());
        }

    }



    private Member createStudent(){
        MemberJoinRequest request = new MemberJoinRequest(
                "학생1", "student_test@email.com", "010-1234-5678", "Password1", MemberType.STUDENT
        );
        return Member.join(request);
    }
    private Member createInstructor(){
        MemberJoinRequest request = new MemberJoinRequest(
                "강사1", "instructor_test@email.com", "010-8765-4321", "Password2", MemberType.INSTRUCTOR
        );
        return Member.join(request);
    }


}