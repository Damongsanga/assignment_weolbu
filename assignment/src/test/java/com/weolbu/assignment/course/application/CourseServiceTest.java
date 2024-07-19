package com.weolbu.assignment.course.application;

import com.weolbu.assignment.auth.application.AuthService;
import com.weolbu.assignment.course.domain.CourseRepository;
import com.weolbu.assignment.member.application.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseEnrollmentProcessor courseEnrollmentProcessor;
    @Mock
    private AuthService authService;
    @Mock
    private MemberService memberService;
    @Mock
    private CourseRepository courseRepository;


}