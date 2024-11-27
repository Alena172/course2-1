package com.example.course2.repositories;

import com.example.course2.models.Certificate;
import com.example.course2.models.Course;
import com.example.course2.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByCourseAndUser(Course course, User user);
}