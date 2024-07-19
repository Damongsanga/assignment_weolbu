package com.weolbu.assignment.course.domain;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.weolbu.assignment.course.dto.CourseInfoDto;
import com.weolbu.assignment.global.config.QuerydslConfig;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.MemberRepository;
import com.weolbu.assignment.member.domain.MemberType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


//@DataJpaTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
@Rollback(value = false)
class CustomCourseRepositoryImplTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private CustomCourseRepositoryImpl customCourseRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void init(){
        List<Member> students = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Member student = memberRepository.save(Member.builder()
                    .email("student"+ i+ "@email.com")
                    .name("학생"+i)
                    .type(MemberType.STUDENT)
                    .password("Password")
                    .build());
            students.add(student);
        }
        List<Member> instructors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Member instructor = memberRepository.save(Member.builder()
                    .email("instructor"+i+"@email.com")
                    .name("강사"+i)
                    .type(MemberType.INSTRUCTOR)
                    .password("Password")
                    .build());
            instructors.add(instructor);
        }

        em.clear();

        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Course course = courseRepository.save(
                    Course.builder()
                            .title("강의"+ i)
                            .price(20000)
                            .currentStudents(0)
                            .maxStudents(30)
                            .instructor(instructors.get(i))
                            .build());
            courses.add(course);
        }

        for (int i = 10; i < 20; i++) {
            Course course = courseRepository.save(
                    Course.builder()
                            .title("강의" + i)
                            .price(20000)
                            .currentStudents(0)
                            .maxStudents(10)
                            .instructor(instructors.get(i-10))
                            .build());
            courses.add(course);
        }

        // 전체 학생수는 0번~9번 강의 모두 동일. 수강 학생수는 0번~9번 갈수록 내림차순.
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 10; j++) {
                courses.get(j).enroll(students.get(i));
            }
        }

        // 학생 0이 10번~19번 강의 수강, 전체 학생수는 11번에서 20번으로 갈수록 오름차순.
        for (int i = 10; i < 20; i++) {
            courses.get(i).enroll(students.get(0));
            courseRepository.save(courses.get(i));
        }

        // 데이터베이스 반
        for (int i = 0; i < 20; i++) {
            courseRepository.save(courses.get(i));
        }

    }

    @Test
    void 자신이_신청하지_않은_강의를_조회할_수_있다(){
        Member member1 = memberRepository.findByEmail("student1@email.com").get();
        Pageable pageable = PageRequest.of(0,4);
        Page<CourseInfoDto> result = customCourseRepository.findNotSignedCourses(member1.getId(), CourseOrder.LATEST, pageable);

        System.out.println(result.getContent().stream().map(CourseInfoDto::getCourseId).toList());

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getContent()).allMatch(dto -> dto.getCourseId() > 10); // Ensure courses are not signed by the member
    }

    // 페이징, 다른 조건들도 검사



}