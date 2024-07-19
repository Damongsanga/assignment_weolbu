package com.weolbu.assignment.member.domain;

import com.weolbu.assignment.member.dto.MemberJoinRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
     void 새로운_멤버를_만들_수_있다(){
        // given
        MemberJoinRequest requestForStudent = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "Password1", MemberType.STUDENT
        );

        MemberJoinRequest requestForInstructor = new MemberJoinRequest(
                "강사1", "instructor@email.com", "010-8765-4321", "Password2", MemberType.INSTRUCTOR
        );

        // when
        Member student = Member.join(requestForStudent);
        Member instructor = Member.join(requestForInstructor);

        // then
        assertThat(student.getType()).isEqualTo(MemberType.STUDENT);
        assertThat(student.getEmail()).isEqualTo("student@email.com");
        assertThat(student.getName()).isEqualTo("학생1");
        assertThat(instructor.getType()).isEqualTo(MemberType.INSTRUCTOR);
        assertThat(instructor.getEmail()).isEqualTo("instructor@email.com");
        assertThat(instructor.getName()).isEqualTo("강사1");

    }

}