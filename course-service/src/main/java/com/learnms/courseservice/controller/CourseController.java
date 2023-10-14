package com.learnms.courseservice.controller;

import com.learnms.courseservice.entity.Course;
import com.learnms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
	private final CourseService courseService;

	@PostMapping
	public Course createCourse(@RequestBody Course course) {
		return courseService.createCourse(course);
	}

	@GetMapping("/{courseId}")
	public Course getCourse(@PathVariable Long courseId) {
		return courseService.getCourse(courseId);
	}

	@GetMapping
	public List<Course> getAllCourses() {
		return courseService.getAllCourses();
	}

	@PutMapping("/{courseId}")
	public Course updateCourse(@PathVariable Long courseId, @RequestBody Course updatedCourse) {
		return courseService.updateCourse(courseId, updatedCourse);
	}

	@DeleteMapping("/{courseId}")
	public void deleteCourse(@PathVariable Long courseId) {
		courseService.deleteCourse(courseId);
	}
}
