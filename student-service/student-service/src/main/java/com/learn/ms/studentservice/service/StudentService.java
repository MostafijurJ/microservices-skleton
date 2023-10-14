package com.learn.ms.studentservice.service;

import com.learn.ms.studentservice.dto.StudentDto;
import com.learn.ms.studentservice.entity.Student;
import com.learn.ms.studentservice.exceptions.StudentNotFoundException;
import com.learn.ms.studentservice.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

	private final StudentRepository studentRepository;

	@Autowired
	public StudentService(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	public StudentDto getStudentById(Long studentId) {
		// Use the repository to fetch a student by ID
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

		// Convert the Student entity to a DTO
		return convertToDto(student);
	}

	public StudentDto createStudent(StudentDto studentDto) {
		// Convert the DTO to a Student entity
		Student student = convertToEntity(studentDto);

		// Save the student entity to the database
		student = studentRepository.save(student);

		// Convert the saved entity back to a DTO
		return convertToDto(student);
	}

	public StudentDto updateStudent(Long studentId, StudentDto studentDto) {
		// Use the repository to fetch the existing student
		Student existingStudent = studentRepository.findById(studentId)
				.orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

		// Update the existing student entity with data from the DTO
		existingStudent.setName(studentDto.getName());
		existingStudent.setEmail(studentDto.getEmail());
		existingStudent.setRollNo(studentDto.getRollNo());

		// Save the updated student entity
		existingStudent = studentRepository.save(existingStudent);

		// Convert the updated entity to a DTO
		return convertToDto(existingStudent);
	}

	public void deleteStudent(Long studentId) {
		// Use the repository to delete the student by ID
		studentRepository.deleteById(studentId);
	}

	private StudentDto convertToDto(Student student) {
		// Convert a Student entity to a StudentDto
		return new StudentDto(student.getName(), student.getEmail(), student.getRollNo(), student.getAddressId());
	}

	private Student convertToEntity(StudentDto studentDto) {
		// Convert a StudentDto to a Student entity
		return new Student(studentDto.getName(), studentDto.getEmail(), studentDto.getRollNo(), studentDto.getAddressId());
	}
}
