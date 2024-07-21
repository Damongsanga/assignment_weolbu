package com.weolbu.assignment.course.domain.repository;

import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.CourseMember;
import com.weolbu.assignment.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {

    boolean existsByCourseAndMember(Course course, Member member);
}
