import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class StudentManagementSystem {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1234";
    private static Connection connection;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Establish database connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            while (true) {
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

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        viewAllStudents();
                        break;
                    case 3:
                        updateStudent();
                        break;
                    case 4:
                        deleteStudent();
                        break;
                    case 5:
                        searchStudentsByGrade();
                        break;
                    case 6:
                        listTopPerformingStudents();
                        break;
                    case 7:
                        calculateAverageScore();
                        break;
                    case 8:
                        System.out.println("Exiting...");
                        connection.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    private static void addStudent() {
        try {
            System.out.println("\n--- Add New Student ---");

            // Input validation
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Age: ");
            int age = scanner.nextInt();
            scanner.nextLine();
            if (age <= 0) {
                System.out.println("Error: Age must be positive.");
                return;
            }

            System.out.print("Email: ");
            String email = scanner.nextLine();
            if (!isValidEmail(email)) {
                System.out.println("Error: Invalid email format.");
                return;
            }

            System.out.print("Grade (A, B, C, D, or F): ");
            String grade = scanner.nextLine().toUpperCase();
            if (!grade.matches("[A-D]|F")) {
                System.out.println("Error: Grade must be A, B, C, D, or F.");
                return;
            }

            System.out.print("Score (0-100): ");
            float score = scanner.nextFloat();
            scanner.nextLine();
            if (score < 0 || score > 100) {
                System.out.println("Error: Score must be between 0 and 100.");
                return;
            }

            // Use transaction for data integrity
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO Student (name, age, email, grade, score) VALUES (?, ?, ?, ?, ?)")) {

                stmt.setString(1, name);
                stmt.setInt(2, age);
                stmt.setString(3, email);
                stmt.setString(4, grade);
                stmt.setFloat(5, score);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Student added successfully!");
                } else {
                    connection.rollback();
                    System.out.println("Failed to add student.");
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Database error: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private static void viewAllStudents() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Student ORDER BY id")) {

            System.out.println("\n--- Student List ---");
            while (rs.next()) {
                printStudentRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void updateStudent() {
        try {
            System.out.println("\n--- Update Student ---");
            System.out.print("Enter student ID to update: ");
            int id = scanner.nextInt();
            scanner.nextLine();

            // Check if student exists
            if (!studentExists(id)) {
                System.out.println("Student with ID " + id + " not found.");
                return;
            }

            System.out.print("New name (leave blank to keep current): ");
            String name = scanner.nextLine();

            System.out.print("New age (enter 0 to keep current): ");
            int age = scanner.nextInt();
            scanner.nextLine();
            if (age < 0) {
                System.out.println("Error: Age must be positive.");
                return;
            }

            System.out.print("New email (leave blank to keep current): ");
            String email = scanner.nextLine();
            if (!email.isEmpty() && !isValidEmail(email)) {
                System.out.println("Error: Invalid email format.");
                return;
            }

            System.out.print("New grade (A, B, C, D, or F, leave blank to keep current): ");
            String grade = scanner.nextLine().toUpperCase();
            if (!grade.isEmpty() && !grade.matches("[A-D]|F")) {
                System.out.println("Error: Grade must be A, B, C, D, or F.");
                return;
            }

            System.out.print("New score (enter -1 to keep current): ");
            float score = scanner.nextFloat();
            scanner.nextLine();
            if (score != -1 && (score < 0 || score > 100)) {
                System.out.println("Error: Score must be between 0 and 100.");
                return;
            }

            // Build dynamic update query
            StringBuilder query = new StringBuilder("UPDATE Student SET ");
            boolean needsComma = false;

            if (!name.isEmpty()) {
                query.append("name = ?");
                needsComma = true;
            }
            if (age > 0) {
                if (needsComma) query.append(", ");
                query.append("age = ?");
                needsComma = true;
            }
            if (!email.isEmpty()) {
                if (needsComma) query.append(", ");
                query.append("email = ?");
                needsComma = true;
            }
            if (!grade.isEmpty()) {
                if (needsComma) query.append(", ");
                query.append("grade = ?");
                needsComma = true;
            }
            if (score != -1) {
                if (needsComma) query.append(", ");
                query.append("score = ?");
            }

            query.append(" WHERE id = ?");

            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
                int paramIndex = 1;

                if (!name.isEmpty()) {
                    stmt.setString(paramIndex++, name);
                }
                if (age > 0) {
                    stmt.setInt(paramIndex++, age);
                }
                if (!email.isEmpty()) {
                    stmt.setString(paramIndex++, email);
                }
                if (!grade.isEmpty()) {
                    stmt.setString(paramIndex++, grade);
                }
                if (score != -1) {
                    stmt.setFloat(paramIndex++, score);
                }

                stmt.setInt(paramIndex, id);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Student updated successfully!");
                } else {
                    connection.rollback();
                    System.out.println("Failed to update student.");
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Database error: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private static void deleteStudent() {
        try {
            System.out.println("\n--- Delete Student ---");
            System.out.print("Enter student ID to delete: ");
            int id = scanner.nextInt();
            scanner.nextLine();

            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM Student WHERE id = ?")) {

                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Student deleted successfully!");
                } else {
                    connection.rollback();
                    System.out.println("Student with ID " + id + " not found.");
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Database error: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private static void searchStudentsByGrade() {
        try {
            System.out.println("\n--- Search Students by Grade ---");
            System.out.print("Enter grade to search (A, B, C, D, or F): ");
            String grade = scanner.nextLine().toUpperCase();

            if (!grade.matches("[A-D]|F")) {
                System.out.println("Error: Grade must be A, B, C, D, or F.");
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM Student WHERE grade = ? ORDER BY score DESC")) {

                stmt.setString(1, grade);
                ResultSet rs = stmt.executeQuery();

                System.out.println("\nStudents with grade " + grade + ":");
                while (rs.next()) {
                    printStudentRecord(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void listTopPerformingStudents() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM Student WHERE score >= 90 ORDER BY score DESC")) {

            System.out.println("\n--- Top-Performing Students (Score >= 90) ---");
            while (rs.next()) {
                printStudentRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void calculateAverageScore() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT AVG(score) FROM Student")) {

            if (rs.next()) {
                System.out.printf("\nAverage score of all students: %.1f\n", rs.getFloat(1));
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    // Helper methods
    private static boolean studentExists(int id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM Student WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private static void printStudentRecord(ResultSet rs) throws SQLException {
        System.out.printf("ID: %d, Name: %s, Age: %d, Email: %s, Grade: %s, Score: %.1f\n",
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("email"),
                rs.getString("grade"),
                rs.getFloat("score"));
    }

    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.matches(emailRegex, email);
    }
}