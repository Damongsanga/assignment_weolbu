package com.weolbu.assignment.course.domain.repository;

import com.weolbu.assignment.course.domain.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, CustomCourseRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c FROM Course c
            WHERE c.id = :id
            """)
    Optional<Course> findByIdForUpdate(long id);

    boolean existsByTitle(String title);
}
