package com.example.course2.repositories;

import com.example.course2.models.Course;
import com.example.course2.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Course c SET c.instructor = NULL WHERE c.instructor = :instructor")
    void updateInstructorToNull(User instructor);


    List<Course> findByInstructor(User instructor);
}
