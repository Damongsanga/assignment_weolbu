package com.weolbu.assignment.course.concurrency;

import com.weolbu.assignment.course.application.CourseService;
import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.repository.CourseMemberRepository;
import com.weolbu.assignment.course.domain.repository.CourseRepository;
import com.weolbu.assignment.course.dto.CourseEnrollRequest;
import com.weolbu.assignment.course.dto.EnrollCourseResponse;
import com.weolbu.assignment.course.dto.EnrollmentResult;
import com.weolbu.assignment.member.application.MemberService;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.repository.MemberRepository;
import com.weolbu.assignment.member.domain.MemberType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SpringBootTest
@TestInstance(value = PER_CLASS)
@ActiveProfiles("test")
public class ConcurrencyTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseMemberRepository courseMemberRepository;


    private Member instructor;
    private Course course1;
    private Course course2;
    private List<Member> members = new ArrayList<>();
    private static final int threadCount = 5;


    @BeforeAll
    @Transactional
    void init(){
        createStudents(threadCount);
        instructor = memberRepository.save(Member.builder().name("강사").email("instructor@email.com").password("Password").type(MemberType.INSTRUCTOR).build());
        course1 = createCourse(3, instructor);
        course2 = createCourse(4, instructor);
    }

    private void createStudents(int n) {
        for (int i = 0; i < n; i++) {
            members.add(Member.builder().name("학생"+i).email("student"+i+"@email.com").password("Password").type(MemberType.STUDENT).build());
            memberRepository.save(members.get(i));
        }
    }

    private Course createCourse(int maxStudents, Member instructor){
        Course course = new Course("강의"+maxStudents, 20000, 0, maxStudents , instructor);
        return courseRepository.save(course);
    }

    @Test
    void 동시에_수강신청_요청이_발생해도_순차적으로_등록된다() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        ConcurrentHashMap<Long, List<Integer>> succeededMemberMap = new ConcurrentHashMap<>();
        succeededMemberMap.put(course1.getId(), new ArrayList<>());
        succeededMemberMap.put(course2.getId(), new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            final int finalI = i;
            executorService.submit(() -> {
                try {
                    setSecurityContext(members.get(finalI));
                    // 수강 신청
                    EnrollCourseResponse response = courseService.enrollCourse(new CourseEnrollRequest(List.of(course1.getId(), course2.getId())));
                    // 결과에 따라 success, fail 카운팅. success인 경우 ConcurrentHashMap에 저장하여 실제로 관계가 생성되었는지 확인
                    for (EnrollmentResult result : response.detail()){
                        if (result.isEnrolled()) {
                            successCount.getAndIncrement();
                            succeededMemberMap.get(result.courseId()).add(finalI);
                        }
                        else {
                            failCount.getAndIncrement();
                        }
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();
        course1 = courseRepository.findById(course1.getId()).get();
        course2 = courseRepository.findById(course2.getId()).get();

        // then
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount * 2);
        assertThat(successCount.get()).isEqualTo(7);
        assertThat(course1.getCurrentStudents()).isEqualTo(3);
        assertThat(course2.getCurrentStudents()).isEqualTo(4);

        // 실제로 수강신청되었는지 확인
        for(int memberIdx : succeededMemberMap.get(course1.getId())){
            System.out.println(members.get(memberIdx).getName());
            assertThat(courseMemberRepository.existsByCourseAndMember(course1, members.get(memberIdx))).isTrue();
        }
        for(int memberIdx : succeededMemberMap.get(course2.getId())){
            System.out.println(members.get(memberIdx).getName());
            assertThat(courseMemberRepository.existsByCourseAndMember(course2, members.get(memberIdx))).isTrue();
        }

    }

    private void setSecurityContext(Member member) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new TestingAuthenticationToken(member.getId(), null, member.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);
    }

}
