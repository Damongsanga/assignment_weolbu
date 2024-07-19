package com.weolbu.assignment.course.domain;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.weolbu.assignment.course.dto.CourseInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.weolbu.assignment.course.domain.QCourse.course;
import static com.weolbu.assignment.course.domain.QCourseMember.courseMember;
import static com.weolbu.assignment.member.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class CustomCourseRepositoryImpl implements CustomCourseRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<CourseInfoDto> findNotSignedCourses(Long memberId, CourseOrder courseOrder, Pageable pageable) {
        QCourseMember subCourseMember = new QCourseMember("subCourseMember");
        JPQLQuery<Long> subQuery = JPAExpressions.select(subCourseMember.course.id)
                .from(subCourseMember)
                .where(subCourseMember.member.id.eq(memberId));

        List<CourseInfoDto> dtos = jpaQueryFactory.select(
                        Projections.constructor(
                                CourseInfoDto.class,
                                course.id,
                                course.title,
                                course.maxStudents,
                                course.currentStudents,
                                course.price,
                                course.instructor.name
                        )
                )
                .from(course)
                .where(course.id.notIn(subQuery))
                .orderBy(makeOrder(courseOrder))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory.select(course.count())
                .from(course)
                .where(course.id.notIn(subQuery))
                .fetchOne();

        long totalCount = Optional.ofNullable(total).orElse(0L);

        return new PageImpl<>(dtos, pageable, totalCount);
    }

    @Override
    public Page<CourseInfoDto> findSignedCourse(Long memberId, CourseOrder courseOrder, Pageable pageable) {
        List<CourseInfoDto> dtos = jpaQueryFactory.select(
                        Projections.constructor(
                                CourseInfoDto.class,
                                course.id,
                                course.title,
                                course.maxStudents,
                                course.currentStudents,
                                course.price,
                                member.name
                        )
                )
                .from(member)
                .leftJoin(member.signedCourse, courseMember)
                .leftJoin(courseMember.course, course)
                .where(member.id.eq(memberId))
                .orderBy(makeOrder(courseOrder))
                .offset(pageable.getOffset())
                .limit(pageable.getPageNumber())
                .fetch();

        Long total = jpaQueryFactory.select(course.count())
                .from(member)
                .leftJoin(member.signedCourse, courseMember)
                .leftJoin(courseMember.course, course)
                .where(member.id.eq(memberId))
                .fetchOne();

        long totalCount = Optional.ofNullable(total).orElse(0L);

        return new PageImpl<>(dtos, pageable, totalCount);
    }

    @Override
    public Page<CourseInfoDto> findManagingCourse(Long memberId, CourseOrder courseOrder, Pageable pageable) {
        List<CourseInfoDto> dtos = jpaQueryFactory.select(
                        Projections.constructor(
                                CourseInfoDto.class,
                                course.id,
                                course.title,
                                course.maxStudents,
                                course.currentStudents,
                                course.price,
                                member.name
                        )
                )
                .from(member)
                .leftJoin(member.managingCourses, course)
                .where(member.id.eq(memberId))
                .orderBy(makeOrder(courseOrder))
                .offset(pageable.getOffset())
                .limit(pageable.getPageNumber())
                .fetch();

        Long total = jpaQueryFactory.select(course.count())
                .from(member)
                .leftJoin(member.managingCourses, course)
                .where(member.id.eq(memberId))
                .fetchOne();

        long totalCount = Optional.ofNullable(total).orElse(0L);

        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private <T> OrderSpecifier<?> makeOrder(CourseOrder courseOrder) {
        return switch (courseOrder) {
            case LATEST -> new OrderSpecifier<>(Order.DESC, course.createdAt);
            case MOSTSIGNEDRATE ->  new OrderSpecifier<>(Order.DESC, Expressions.numberTemplate(Double.class, "{0} / {1}", course.currentStudents, course.maxStudents));
            case MOSTSIGNEDSTUDENT -> new OrderSpecifier<>(Order.DESC, course.currentStudents);
        };
    }
}
