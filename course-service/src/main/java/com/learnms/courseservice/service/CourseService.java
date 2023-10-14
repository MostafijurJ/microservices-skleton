package com.learnms.courseservice.service;

import com.learnms.courseservice.entity.Course;
import com.learnms.courseservice.exceptions.ResourceNotFoundException;
import com.learnms.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
	private final CourseRepository courseRepository; // Assuming you have a CourseRepository.

	public Course createCourse(Course course) {
		return courseRepository.save(course);
	}

	public Course getCourse(Long courseId) {
		return courseRepository.findById(courseId)
				.orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
	}

	public List<Course> getAllCourses() {
		return courseRepository.findAll();
	}

	public Course updateCourse(Long courseId, Course updatedCourse) {
		return courseRepository.findById(courseId)
				.map(course -> {
					// Update the course fields here
					course.setName(updatedCourse.getName());
					course.setDescription(updatedCourse.getDescription());
					course.setInstructor(updatedCourse.getInstructor());
					course.setDepartment(updatedCourse.getDepartment());
					return courseRepository.save(course);
				})
				.orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
	}

	public void deleteCourse(Long courseId) {
		courseRepository.deleteById(courseId);
	}
}
