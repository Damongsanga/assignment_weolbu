package com.weolbu.assignment.course.domain;

import com.weolbu.assignment.course.dto.CourseCreateRequest;
import com.weolbu.assignment.global.BaseEntity;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.MemberType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "course")
public class Course extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private Integer maxStudents;

    @Column(nullable = false)
    private Integer currentStudents;

    @Column(nullable = false)
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CourseMember> students = new ArrayList<>();

    @Builder
    public Course(String title, Integer price, Integer currentStudents, Integer maxStudents, Member instructor) {
        this.title = title;
        this.price = price;
        this.currentStudents = currentStudents;
        this.maxStudents = maxStudents;
        this.instructor = instructor;
    }


    public static Course of(CourseCreateRequest request, Member instructor){

        if (!instructor.getType().equals(MemberType.INSTRUCTOR)){
            throw new BaseException(NOT_INSTRUCTOR);
        }

        return Course.builder()
                .title(request.title())
                .maxStudents(request.maxStudents())
                .currentStudents(0)
                .price(request.price())
                .instructor(instructor)
                .build();
    }

    public void enroll(Member member) {
        if (instructor.getId() == member.getId()) throw new BaseException(SELF_ENROLLMENT);
        if (currentStudents >= maxStudents) throw new BaseException(FULL_COURSE);
        CourseMember courseMember = new CourseMember(this, member);
        students.add(courseMember);
        currentStudents++;
    }

}
