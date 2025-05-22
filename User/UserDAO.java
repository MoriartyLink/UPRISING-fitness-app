package project.uprising.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Check if a phone number is already registered
    public boolean phoneExists(String phoneNo) {
        String sql = "SELECT COUNT(*) FROM users WHERE phone_no = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // if count > 0, phone is in use
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Create user (Sign Up)
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, phone_no, password, email, gender, date_of_birth, height, weight, goal, experience, workoutDaysPerWeek, exp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPhoneNo());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getGender());
            stmt.setDate(6, Date.valueOf(user.getDateOfBirth()));
            stmt.setInt(7, user.getHeight());
            stmt.setDouble(8, user.getWeight());
            stmt.setString(9, user.getGoal()); // Add goal
            stmt.setString(10, user.getExperience());
            stmt.setInt(11, user.getWorkoutDaysPerWeek());
            int initialExp = switch (user.getExperience()) {
                case "Intermediate (1-3 years)" -> 2000;
                case "Advanced (3+ years)" -> 6000;
                default -> 0;
            };
            stmt.setInt(12, initialExp);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("User created with EXP: " + initialExp + ", Workout Days: " + user.getWorkoutDaysPerWeek() + ", Goal: " + user.getGoal() + ", Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1)); // Set the generated UserID
                    }
                }
                return true;
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateUserExp(int userId, int exp) {
        String sql = "UPDATE users SET exp = ? WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exp);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Login: fetch user by phone and password
    public User getUserByPhoneAndPassword(String phoneNo, String password) {
        String sql = "SELECT * FROM users WHERE phone_no = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNo);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("UserID"));
                    user.setUsername(rs.getString("username"));
                    user.setPhoneNo(rs.getString("phone_no"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setGender(rs.getString("gender"));
                    user.setDateOfBirth(LocalDate.parse(rs.getString("date_of_birth"))); // Parse date from String
                    user.setHeight(rs.getInt("height")); //
                    user.setExp(rs.getInt("exp"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean logProgress(int userId, String activityType, int activityId, LocalDate date, int dummy) {
        String sql = "INSERT INTO user_progress (user_id, activity_type, exercise_id, challenge_id, program_id, completion_date) " +
                "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE completion_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, activityType);
            if ("exercise".equals(activityType)) {
                stmt.setInt(3, activityId);
                stmt.setNull(4, java.sql.Types.INTEGER); // challenge_id
                stmt.setNull(5, java.sql.Types.INTEGER); // program_id
            } else if ("challenge".equals(activityType)) {
                stmt.setNull(3, java.sql.Types.INTEGER); // exercise_id
                stmt.setInt(4, activityId); // challenge_id
                stmt.setNull(5, java.sql.Types.INTEGER); // program_id
            } else if ("program".equals(activityType)) {
                stmt.setNull(3, java.sql.Types.INTEGER); // exercise_id
                stmt.setNull(4, java.sql.Types.INTEGER); // challenge_id
                stmt.setInt(5, activityId); // program_id
            } else {
                return false; // Unknown type
            }
            stmt.setDate(6, java.sql.Date.valueOf(date));
            stmt.setDate(7, java.sql.Date.valueOf(date));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkProgressForDate(int userId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE user_id = ? AND completion_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // True if any progress exists for the date
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<String> getProgressDetails(int userId, LocalDate date) {
        List<String> details = new ArrayList<>();
        String sql = "SELECT e.exerciseTitle, e.targetMuscles " +
                "FROM user_progress up " +
                "JOIN Exercise e ON up.exercise_id = e.ExerciseID " +
                "WHERE up.user_id = ? AND up.completion_date = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                details.add(rs.getString("exerciseTitle") + " (" + rs.getString("targetMuscles") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public int getExerciseCountForDate(int userId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE user_id = ? AND completion_date = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the user ID and date parameters
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // Check if the result set has data
            if (rs.next()) {
                // Return the count of exercises for the given date
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Handle any database errors
            System.err.println("Error fetching exercise count for date: " + e.getMessage());
            e.printStackTrace();
        }

        // Return 0 if no exercises are found or an error occurs
        return 0;
    }

    public boolean updateUser(User currentUser) {
        String sql = "UPDATE users SET exp = ?, last_active_date = ? WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getExp());
            // Handle null lastActiveDate if necessary
            stmt.setDate(2, currentUser.getLastActiveDate() != null ? Date.valueOf(currentUser.getLastActiveDate()) : null);
            stmt.setInt(3, currentUser.getId());
            return stmt.executeUpdate() > 0; // Returns true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public int getExerciseStreak(int userId) {
        String sql = "SELECT DISTINCT completion_date FROM user_progress " +
                "WHERE user_id = ? AND activity_type = 'exercise' " +
                "ORDER BY completion_date DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            List<LocalDate> exerciseDates = new ArrayList<>();
            while (rs.next()) {
                exerciseDates.add(rs.getDate("completion_date").toLocalDate());
            }

            if (exerciseDates.isEmpty()) {
                return 0;
            }

            // Start from the most recent date
            LocalDate currentDate = exerciseDates.get(0);
            int streak = 1;

            // Check for consecutive days
            for (int i = 1; i < exerciseDates.size(); i++) {
                LocalDate previousDate = exerciseDates.get(i);
                if (previousDate.equals(currentDate.minusDays(1))) {
                    streak++;
                    currentDate = previousDate;
                } else {
                    break; // Streak is broken
                }
            }

            // Check if the streak includes today or yesterday
            LocalDate today = LocalDate.now();
            if (currentDate.equals(today) || currentDate.equals(today.minusDays(1))) {
                return streak;
            } else {
                return 0; // Streak is not current
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getExerciseAttemptsToday(int userId, int exerciseId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE user_id = ? AND exercise_id = ? AND completion_date = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, exerciseId);
            stmt.setDate(3, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int calculateLevel(int exp) {
        if (exp >= 6000) return 3; // Advanced
        if (exp >= 2000) return 2; // Intermediate
        return 1; // Beginner
    }

    public List<User> getUsersByExperienceAndGoal(String experience, String goal) {
        List<User> users = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<String> params = new ArrayList<>();

        // Add experience filter if provided
        if (experience != null && !experience.isEmpty()) {
            sql.append(" AND experience = ?");
            params.add(experience);
        }
        // Add goal filter if provided
        if (goal != null && !goal.isEmpty()) {
            sql.append(" AND goal = ?");
            params.add(goal);
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters for the prepared statement
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("UserID"));
                user.setUsername(rs.getString("username"));
                user.setPhoneNo(rs.getString("phone_no"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setGender(rs.getString("gender"));
                user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                user.setHeight(rs.getInt("height"));
                user.setWeight(rs.getDouble("weight"));
                user.setGoal(rs.getString("goal"));
                user.setExperience(rs.getString("experience"));
                user.setExp(rs.getInt("exp"));
                user.setWorkoutDaysPerWeek(rs.getInt("workoutDaysPerWeek"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error filtering users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET username = ?, phone_no = ?, email = ?, gender = ?, date_of_birth = ?, height = ?, weight = ?, goal = ?, workoutdaysperweek = ? WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPhoneNo());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getGender());
            stmt.setDate(5, Date.valueOf(user.getDateOfBirth())); // Assumes date_of_birth is stored as a DATE in the database
            stmt.setInt(6, user.getHeight());
            stmt.setDouble(7, user.getWeight());
            stmt.setString(8, user.getGoal());
            stmt.setInt(9, user.getWorkoutDaysPerWeek());
            stmt.setInt(10, user.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getUserLevel(int userId) throws SQLException {
        String sql = "SELECT experience FROM users WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String experience = rs.getString("experience");
                if (experience == null) return 1; // Default to Beginner
                switch (experience.toLowerCase()) {
                    case "beginner": return 1;
                    case "intermediate": return 2;
                    case "advanced": return 3;
                    default: return 1; // Default to Beginner
                }
            }
        }
        return 1; // Default if user not found
    }
    private int getLevelForExperience(String experience) {
        switch (experience.toLowerCase()) {
            case "beginner": return 1;
            case "intermediate": return 2;
            case "advanced": return 3;
            default: return 1;
        }

    }
    public List<String> getRecentWorkoutHistory(int userId) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT e.exerciseTitle, up.completion_date " +
                "FROM user_progress up " +
                "JOIN Exercise e ON up.exercise_id = e.ExerciseID " +
                "WHERE up.user_id = ? AND up.activity_type = 'exercise' " +
                "ORDER BY up.completion_date DESC " +
                "LIMIT 5";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String entry = rs.getString("exerciseTitle") + " - " +
                        rs.getDate("completion_date").toLocalDate().toString();
                history.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }



    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE UserID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("UserID"));
                user.setUsername(rs.getString("username"));
                user.setPhoneNo(rs.getString("phone_no"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setGender(rs.getString("gender"));
                user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                user.setHeight(rs.getInt("height"));
                user.setWeight(rs.getDouble("weight"));
                user.setGoal(rs.getString("goal"));
                user.setExperience(rs.getString("experience"));
                user.setWorkoutDaysPerWeek(rs.getInt("workoutDaysPerWeek"));
                user.setExp(rs.getInt("exp"));
                // Optional: set lastActiveDate if part of your User class
                // user.setLastActiveDate(rs.getDate("last_active_date") != null ? rs.getDate("last_active_date").toLocalDate() : null);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Return null if user not found or error occurs
    }
}
