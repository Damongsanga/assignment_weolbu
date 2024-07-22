package com.weolbu.assignment.course.domain;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.weolbu.assignment.course.domain.repository.CourseRepository;
import com.weolbu.assignment.course.domain.repository.CustomCourseRepositoryImpl;
import com.weolbu.assignment.course.dto.CourseInfoDto;
import com.weolbu.assignment.global.config.QuerydslConfig;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.repository.MemberRepository;
import com.weolbu.assignment.member.domain.MemberType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
@Transactional
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
        Member student = new Member("학생","Password","student_test@email.com", MemberType.STUDENT);
        Member instructor = new Member("강사", "Password", "instructor_test@email.com",MemberType.INSTRUCTOR);
        Member instructor2 = new Member("강사2", "Password", "instructor2_test@email.com",MemberType.INSTRUCTOR);

        em.persist(student);
        em.persist(instructor);
        em.persist(instructor2);

        List<Course> courses = Arrays.asList(
                new Course("test_강의1", 20000, 10, 30, instructor),
                new Course("test_강의2", 20000, 2, 10, instructor),
                new Course("test_강의3", 20000, 8, 8, instructor),
                new Course("test_강의4", 20000, 2, 100, instructor),
                new Course("test_강의5", 20000, 1, 50, instructor),
                new Course("test_강의6", 20000, 1, 50, instructor2),
                new Course("test_강의7", 20000, 2, 100, instructor2),
                new Course("test_강의8", 20000, 8, 8, instructor2),
                new Course("test_강의9", 20000, 2, 10, instructor2),
                new Course("test_강의10", 20000, 10, 30, instructor2)
        );
        courses.forEach(em::persist);

        Stream.of(courses.get(4), courses.get(6), courses.get(7), courses.get(8), courses.get(9)).map(c -> new CourseMember(c, student))
                .forEach(cm -> em.persist(cm));

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("전체 강의 조회")
    class All {
        @Test
        void 전체_강의를_최신순으로_조회할_수_있다(){
            // given
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findAllCourses(CourseOrder.LATEST, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의10", "test_강의9", "test_강의8", "test_강의7");
        }

        @Test
        void 전체_강의를_인기순으로_조회할_수_있다(){
            // given
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findAllCourses(CourseOrder.RATE, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의8", "test_강의3", "test_강의10", "test_강의1");
        }

        @Test
        void 전체_강의를_학생수_순으로_조회할_수_있다(){
            // given
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findAllCourses(CourseOrder.CURRENT, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의10", "test_강의1", "test_강의8", "test_강의3");
        }

    }
    @Nested
    @DisplayName("수강 신청하지 않은 강의 조회")
    class Unsigned {
        @Test
        void 자신이_신청하지_않은_강의를_최신순으로_조회할_수_있다(){
            // given
            Member student = memberRepository.findByEmail("student_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findUnenrolledCourses(student.getId(), CourseOrder.LATEST, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의6", "test_강의4", "test_강의3", "test_강의2");
        }

        @Test
        void 자신이_신청하지_않은_강의를_인기순으로_조회할_수_있다(){
            // given
            Member student = memberRepository.findByEmail("student_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findUnenrolledCourses(student.getId(), CourseOrder.RATE, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의3", "test_강의1", "test_강의2", "test_강의6");
        }

        @Test
        void 자신이_신청하지_않은_강의를_학생수_순으로_조회할_수_있다(){
            // given
            Member student = memberRepository.findByEmail("student_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findUnenrolledCourses(student.getId(), CourseOrder.CURRENT, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의1", "test_강의3", "test_강의4", "test_강의2");
        }
    }
    @Nested
    @DisplayName("수강 신청한 강의 조회")
    class Signed {

        @Test
        void 자신이_신청한_강의를_최신순으로_조회할_수_있다(){
            // given
            Member student = memberRepository.findByEmail("student_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findEnrolledCourses(student.getId(), CourseOrder.LATEST, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의10", "test_강의9", "test_강의8", "test_강의7");
        }

        @Test
        void 자신이_신청한_강의를_인기순으로_조회할_수_있다(){
            // given
            Member student = memberRepository.findByEmail("student_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findEnrolledCourses(student.getId(), CourseOrder.RATE, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의8", "test_강의10", "test_강의9", "test_강의7");
        }

        @Test
        void 자신이_신청한_강의를_학생수_순으로_조회할_수_있다(){
            // given
            Member student = memberRepository.findByEmail("student_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findEnrolledCourses(student.getId(), CourseOrder.CURRENT, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의10", "test_강의8", "test_강의9", "test_강의7");
        }
    }
    @Nested
    @DisplayName("자신이 생성한 강의 조회")
    class Managed {

        @Test
        void 강사는_자신이_개설한_강의를_최신순으로_조회할_수_있다(){
            // given
            Member instructor = memberRepository.findByEmail("instructor_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findManagingCourses(instructor.getId(), CourseOrder.LATEST, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의5", "test_강의4", "test_강의3", "test_강의2");
        }

        @Test
        void 강사는_자신이_개설한_강의를_인기순으로_조회할_수_있다(){
            // given
            Member instructor = memberRepository.findByEmail("instructor_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findManagingCourses(instructor.getId(), CourseOrder.RATE, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의3", "test_강의1", "test_강의2", "test_강의5");
        }

        @Test
        void 강사는_자신이_개설한_강의를_학생수_순으로_조회할_수_있다(){
            // given
            Member instructor = memberRepository.findByEmail("instructor_test@email.com").get();
            Pageable pageable = PageRequest.of(0,4);

            // when
            Page<CourseInfoDto> result = customCourseRepository.findManagingCourses(instructor.getId(), CourseOrder.CURRENT, pageable);
            List<String> courseTitles = result.getContent().stream().map(CourseInfoDto::getTitle).toList();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getNumberOfElements()).isEqualTo(4);
            assertThat(courseTitles).containsExactly("test_강의1", "test_강의3", "test_강의4", "test_강의2");
        }

    }




}