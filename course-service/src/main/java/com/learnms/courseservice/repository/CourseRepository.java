package com.learnms.courseservice.repository;

import com.learnms.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
	// You can add custom query methods here if needed.
}
