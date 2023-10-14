package com.learn.ms.studentservice.repository;

import com.learn.ms.studentservice.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
	List<Student> findByName(String name);
}
