package com.weolbu.assignment.global.util;

import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.repository.CourseRepository;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.repository.MemberRepository;
import com.weolbu.assignment.member.domain.MemberType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("spring.security.password.salt")
    private String salt;

    @Value("${app.data-initializer.enabled:true}")
    private boolean isEnabled;

    @Override
    @Transactional
    public void run(String... args) {

        if (!isEnabled) return;
        if (memberRepository.existsByEmail("student@email.com")) return;

        Member testMember = memberRepository.save(new Member("학생", passwordEncoder.encode("Password"+salt), "student@email.com", MemberType.STUDENT));

        for (int i = 1; i <= 30; i++) {
            memberRepository.save(new Member("학생"+i, passwordEncoder.encode("Password"+salt), "student"+i+"@email.com", MemberType.STUDENT));
        }

        Member instructor = memberRepository.save(new Member("강사1", passwordEncoder.encode("Password"+salt), "instructor1"+"@email.com", MemberType.INSTRUCTOR));
        Member instructor2 = memberRepository.save(new Member("강사2", passwordEncoder.encode("Password"+salt), "instructor2"+"@email.com", MemberType.INSTRUCTOR));

        // 1 ~ 5 번 강의 instructor
        List<Course> courses = Arrays.asList(
                new Course("강의1", 20000, 0, 8, instructor),
                new Course("강의2", 20000, 0, 8, instructor),
                new Course("강의3", 20000, 0, 8, instructor),
                new Course("강의4", 20000, 0, 8, instructor),
                new Course("강의5", 20000, 0, 8, instructor),
                new Course("강의6", 20000, 0, 4, instructor2),
                new Course("강의7", 20000, 0, 2, instructor2),
                new Course("강의8", 20000, 0, 3, instructor2),
                new Course("강의9", 20000, 0, 5, instructor2),
                new Course("강의10", 20000, 0, 6, instructor2),
                new Course("강의11", 20000, 0, 8, instructor),
                new Course("강의12", 20000, 0, 8, instructor),
                new Course("강의13", 20000, 0, 8, instructor),
                new Course("강의14", 20000, 0, 8, instructor),
                new Course("강의15", 20000, 0, 8, instructor),
                new Course("강의16", 20000, 0, 4, instructor2),
                new Course("강의17", 20000, 0, 2, instructor2),
                new Course("강의18", 20000, 0, 3, instructor2),
                new Course("강의19", 20000, 0, 5, instructor2),
                new Course("강의20", 20000, 0, 6, instructor2)
        );

        courses.forEach(c -> courseRepository.save(c));
        courses = courseRepository.findAll();

        // 1~5 : 테스트용 멤버 수강 X, maxStudents 8로 동일, currentMember 1~5로 내림차순
        for (int i = 0; i < 5; i++) {
            for (int j = 5-i; j > 0; j--) {
                courses.get(i).enroll(memberRepository.findByEmail("student"+j+"@email.com").orElseThrow(() -> new BaseException(NO_MEMBER)));
            }
        }

        // 6~10 : 테스트용 멤버 수강 X, maxStudents 4,2,3,5,6으로 다름, currentMember 1로 동일
        for (int i = 5; i < 10; i++) {
            courses.get(i).enroll(memberRepository.findByEmail("student"+1+"@email.com").orElseThrow(() -> new BaseException(NO_MEMBER)));
        }

        // 11~15 : 테스트용 멤버 수강 X, maxStudents 8로 동일, currentMember 11~15로 내림차순
        for (int i = 10; i < 15; i++) {
            courses.get(i).enroll(testMember);
            for (int j = 5-i; j > 0; j--) {
                courses.get(i).enroll(memberRepository.findByEmail("student"+j+"@email.com").orElseThrow(() -> new BaseException(NO_MEMBER)));
            }
        }

        // 16~20 : 테스트용 멤버 수강 O, maxStudents 4,2,3,5,6으로 다름, currentMember 1로 동일
        for (int i = 15; i < 20; i++) {
            courses.get(i).enroll(testMember);
        }

    }
}
