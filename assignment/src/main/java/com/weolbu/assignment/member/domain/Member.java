package com.weolbu.assignment.member.domain;

import com.weolbu.assignment.course.domain.Course;
import com.weolbu.assignment.course.domain.CourseMember;
import com.weolbu.assignment.global.config.BaseEntity;
import com.weolbu.assignment.member.dto.MemberJoinRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "member")
public class Member extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType type;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<CourseMember> signedCourse = new ArrayList<>();

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    private List<Course> managingCourses = new ArrayList<>();

    @Builder
    public Member(String password, String name, String email, MemberType type) {
        this.password = password;
        this.name = name;
        this.email = email;
        this.type = type;
    }

    public static Member join(MemberJoinRequest request) {
        return Member.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .type(request.type())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(type.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.id+"";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
