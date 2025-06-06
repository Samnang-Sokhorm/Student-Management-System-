package view;

import java.util.List;
import java.util.Scanner;
import Model.Student;

public class StudentView {
    private Scanner scanner;

    public StudentView() {
        scanner = new Scanner(System.in);
    }

    public void displayMenu() {
        System.out.println("\n=== Student Management System ===");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Update Student");
        System.out.println("4. Delete Student");
        System.out.println("5. Search Students by Grade");
        System.out.println("6. List Top-Performing Students");
        System.out.println("7. Calculate Average Score");
        System.out.println("8. Exit");
        System.out.print("Enter your choice: ");
    }

    public Student getStudentDetails() {
        System.out.println("\n--- Add New Student ---");
        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Age: ");
        int age = Integer.parseInt(scanner.nextLine());

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Grade (A, B, C, D, or F): ");
        String grade = scanner.nextLine().toUpperCase();

        System.out.print("Score (0-100): ");
        float score = Float.parseFloat(scanner.nextLine());

        return new Student(0, name, age, email, grade, score);
    }

    public void showAllStudents(List<Student> students) {
        System.out.println("\n--- Student List ---");
        if (students.isEmpty()) {
            System.out.println("No students found.");
        } else {
            for (Student student : students) {
                System.out.println(student);
            }
        }
    }

    public int getStudentId(String action) {
        System.out.printf("\n--- %s Student ---\n", action);
        System.out.print("Enter student ID: ");
        return getIntInput();
    }
    public Student getUpdatedStudentDetails(Student existingStudent) {
        System.out.println("\nCurrent student details:");
        System.out.println(existingStudent);
        System.out.println("\nEnter new details (leave blank to keep current):");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Age: ");
        String ageStr = scanner.nextLine();
        int age = ageStr.isEmpty() ? existingStudent.getAge() : Integer.parseInt(ageStr);

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Grade (A, B, C, D, or F): ");
        String grade = scanner.nextLine().toUpperCase();

        System.out.print("Score (0-100): ");
        String scoreStr = scanner.nextLine();
        float score = scoreStr.isEmpty() ? existingStudent.getScore() : Float.parseFloat(scoreStr);

        return new Student(
                existingStudent.getId(),
                name.isEmpty() ? existingStudent.getName() : name,
                age,
                email.isEmpty() ? existingStudent.getEmail() : email,
                grade.isEmpty() ? existingStudent.getGrade() : grade,
                score
        );
    }

    public String getGradeToSearch() {
        System.out.println("\n--- Search Students by Grade ---");
        System.out.print("Enter grade to search (A, B, C, D, or F): ");
        return scanner.nextLine().toUpperCase();
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String error) {
        System.err.println("Error: " + error);
    }

    public void showAverageScore(float average) {
        System.out.printf("\nAverage score of all students: %.1f\n", average);
    }

    public void close() {
        scanner.close();
    }


    public int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    return Integer.parseInt(input);
                }
                System.out.print("Input cannot be empty. Please enter a number: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}