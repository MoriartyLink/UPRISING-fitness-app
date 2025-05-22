package project.uprising;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import project.uprising.Challenge.Challenge;
import project.uprising.Challenge.ChallengeDAO;
import project.uprising.User.User;
import project.uprising.User.UserDAO;
import project.uprising.User.Session;

import java.time.LocalDate;
import java.time.YearMonth;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressController {
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private VBox historyPane;
    @FXML private Label historyTitle;
    @FXML private Label workoutTitle;
    @FXML private Label workoutDetails;
    @FXML private Label challengeTitle;
    @FXML private Label challengeDetails;
    @FXML private Label programTitle;
    @FXML private Label programDetails;
    @FXML private Button workoutButton;
    @FXML private Button challengeButton;
    @FXML private Button programButton;
    @FXML private VBox workoutSection;
    @FXML private VBox challengeSection;
    @FXML private VBox programSection;

    private LocalDate currentMonthDate = LocalDate.now().withDayOfMonth(1);
    private UserDAO userDao = new UserDAO();
    private String currentMode = "workout";
    private static final Color[] CHALLENGE_COLORS = {Color.BLUE, Color.PURPLE, Color.DARKCYAN, Color.TEAL, Color.ALICEBLUE};
    private Map<Integer, ChallengeInfo> selectedChallenges = new HashMap<>();
    private Integer selectedChallengeId = null;

    @FXML
    public void initialize() {
        setupCalendarHeaders();
        displayCalendar();
        checkConsistencyBonus();
        checkInactiveChallenges();
        updateButtonStyles();
        updateSidePanel();
    }

    private void setupCalendarHeaders() {
        calendarGrid.getChildren().clear();
        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 0; i < 7; i++) {
            Label header = new Label(days[i]);
            header.getStyleClass().add("header-label");
            calendarGrid.add(header, i, 0);
        }
    }

    private void displayCalendar() {
        calendarGrid.getChildren().clear();
        setupCalendarHeaders();

        YearMonth yearMonth = YearMonth.from(currentMonthDate);
        LocalDate firstDay = yearMonth.atDay(1);
        int dayOffset = firstDay.getDayOfWeek().getValue() % 7;

        int row = 1;
        for (int i = 0; i < 42; i++) {
            LocalDate date = firstDay.minusDays(dayOffset).plusDays(i);
            StackPane cell = createCalendarCell(date);
            int column = i % 7;
            if (i > 0 && i % 7 == 0) row++;
            calendarGrid.add(cell, column, row);
        }

        monthLabel.setText(yearMonth.getMonth().toString() + " " + yearMonth.getYear());
    }

    private StackPane createCalendarCell(LocalDate date) {
        StackPane cell = new StackPane();
        cell.getStyleClass().add("calendar-cell");

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("day-label");

        // Highlight the entire cell for progress
        if ("workout".equals(currentMode) && hasProgress(date, "exercise")) {
            Rectangle highlight = new Rectangle(50, 50, Color.GREEN.deriveColor(0, 1, 1, 0.5)); // Semi-transparent green
            highlight.setArcWidth(50); // Full circle for rounding
            highlight.setArcHeight(50);
            cell.getChildren().add(0, highlight);
        } else if ("program".equals(currentMode) && hasProgress(date, "program")) {
            Rectangle highlight = new Rectangle(50, 50, Color.BLUE.deriveColor(0, 1, 1, 0.5)); // Semi-transparent blue
            highlight.setArcWidth(50);
            highlight.setArcHeight(50);
            cell.getChildren().add(0, highlight);
        } else if ("challenge".equals(currentMode)) {
            Map<Integer, ChallengeInfo> challenges = getActiveChallenges();
            int colorIndex = 0;
            for (Map.Entry<Integer, ChallengeInfo> entry : challenges.entrySet()) {
                if (colorIndex >= CHALLENGE_COLORS.length) break;
                ChallengeInfo info = entry.getValue();
                LocalDate startDate = info.startDate;
                int duration = info.duration;
                if (date.isAfter(startDate.minusDays(1)) && date.isBefore(startDate.plusDays(duration))) {
                    if (selectedChallengeId == null || selectedChallengeId.equals(entry.getKey())) {
                        boolean completed = isChallengeCompletedOnDate(entry.getKey(), date);
                        Color color = completed ? CHALLENGE_COLORS[colorIndex].deriveColor(0, 1, 1, 0.5) : Color.PURPLE.deriveColor(0, 1, 1, 0.5);
                        Rectangle highlight = new Rectangle(50, 50, color); // Full cell highlight
                        highlight.setArcWidth(50);
                        highlight.setArcHeight(50);
                        cell.getChildren().add(0, highlight);
                    }
                }
                colorIndex++;
            }
        }

        // Highlight current day
        if (date.isEqual(LocalDate.now())) {
            cell.getStyleClass().add("current-day");
        }
        // Mark inactive days
        if (date.getMonth() != currentMonthDate.getMonth()) {
            cell.getStyleClass().add("inactive-day");
        }

        cell.getChildren().add(dayLabel);
        cell.setOnMouseClicked(e -> {
            if ("challenge".equals(currentMode)) {
                selectedChallengeId = null;
                updateSidePanel();
                displayCalendar();
            } else {
                showProgressDetails(date);
            }
        });
        Tooltip.install(cell, new Tooltip(getProgressTooltip(date)));
        return cell;
    }

    @FXML
    private void nextMonth() {
        currentMonthDate = currentMonthDate.plusMonths(1);
        displayCalendar();
    }

    @FXML
    private void previousMonth() {
        currentMonthDate = currentMonthDate.minusMonths(1);
        displayCalendar();
    }

    @FXML
    private void switchToWorkout() {
        currentMode = "workout";
        selectedChallengeId = null;
        updateButtonStyles();
        displayCalendar();
        historyPane.getChildren().clear(); // Clear previous content
        updateSidePanel();
    }

    @FXML
    private void switchToChallenge() {
        currentMode = "challenge";
        selectedChallengeId = null;
        updateButtonStyles();
        displayCalendar();
        historyPane.getChildren().clear(); // Clear previous content
        updateSidePanel();
    }

    @FXML
    private void switchToProgram() {
        currentMode = "program";
        selectedChallengeId = null;
        updateButtonStyles();
        displayCalendar();
        historyPane.getChildren().clear(); // Clear previous content
        updateSidePanel();
    }

    private void updateButtonStyles() {
        workoutButton.setStyle("-fx-background-color: " + ("workout".equals(currentMode) ? "#2cffff" : "#666666") + "; -fx-text-fill: #000000;");
        challengeButton.setStyle("-fx-background-color: " + ("challenge".equals(currentMode) ? "#0078ff" : "#666666") + "; -fx-text-fill: white;");
        programButton.setStyle("-fx-background-color: " + ("program".equals(currentMode) ? "#9247ff" : "#666666") + "; -fx-text-fill: white;");
    }

    private void updateSidePanel() {
        workoutSection.setVisible("workout".equals(currentMode));
        workoutSection.setManaged("workout".equals(currentMode));
        challengeSection.setVisible("challenge".equals(currentMode));
        challengeSection.setManaged("challenge".equals(currentMode));
        programSection.setVisible("program".equals(currentMode));
        programSection.setManaged("program".equals(currentMode));
        historyTitle.setText(currentMode.substring(0, 1).toUpperCase() + currentMode.substring(1) + " History");

        historyPane.getChildren().clear(); // Clear existing content
        historyPane.getChildren().add(historyTitle); // Add title back

        if ("challenge".equals(currentMode)) {
            displayChallengeCards(); // Keep challenge history unchanged
        } else if ("workout".equals(currentMode)) {
            displayWorkoutHistory(); // New method for workout history
        } else {
            showProgressDetails(LocalDate.now()); // Default for program mode
        }
    }

    private void displayWorkoutHistory() {
        int userId = Session.getCurrentUserID();
        List<String> workoutHistory = userDao.getRecentWorkoutHistory(userId);

        if (workoutHistory.isEmpty()) {
            Label noWorkouts = new Label("No workouts completed yet.");
            noWorkouts.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            historyPane.getChildren().add(noWorkouts);
        } else {
            for (String workout : workoutHistory) {
                Label workoutLabel = new Label(workout);
                workoutLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                historyPane.getChildren().add(workoutLabel);
            }
        }
    }
    private void displayChallengeCards() {
        Map<Integer, ChallengeInfo> challenges = getActiveChallenges();
        selectedChallenges = challenges;

        if (challenges.isEmpty()) {
            Label noChallenges = new Label("No active challenges.");
            noChallenges.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
            historyPane.getChildren().add(noChallenges);
            return;
        }

        int colorIndex = 0;
        for (Map.Entry<Integer, ChallengeInfo> entry : challenges.entrySet()) {
            if (colorIndex >= CHALLENGE_COLORS.length) break;
            int challengeId = entry.getKey();
            String title = getChallengeTitle(challengeId);
            int duration = entry.getValue().duration;
            Color cardColor = CHALLENGE_COLORS[colorIndex];

            Button card = new Button(title + " - " + duration + " days");
            card.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 10; -fx-pref-width: 300px; -fx-pref-height: 40px;",
                    toRGBCode(cardColor)));
            card.setOnAction(e -> {
                selectedChallengeId = challengeId;
                displayChallengeDetails(challengeId, cardColor);
                displayCalendar();
            });
            historyPane.getChildren().add(card);
            colorIndex++;
        }

        if (selectedChallengeId == null) {
            Label prompt = new Label("Select a challenge to view details.");
            prompt.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            historyPane.getChildren().add(prompt);
        }
    }

    private void displayChallengeDetails(int challengeId, Color color) {
        historyPane.getChildren().clear();
        historyPane.getChildren().add(historyTitle);

        ChallengeInfo info = selectedChallenges.get(challengeId);
        if (info == null) return;

        LocalDate startDate = info.startDate;
        LocalDate endDate = startDate.plusDays(info.duration - 1);
        String title = getChallengeTitle(challengeId);

        Label details = new Label(String.format("%s\nStart Date: %s\nEnd Date: %s", title, startDate, endDate));
        details.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 10;",
                toRGBCode(color)));
        details.setWrapText(true);
        details.setPrefWidth(400);
        historyPane.getChildren().add(details);
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private boolean hasProgress(LocalDate date, String activityType) {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE user_id = ? AND completion_date = ? AND activity_type = ?";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getCurrentUserID());
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setString(3, activityType);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<Integer, ChallengeInfo> getActiveChallenges() {
        Map<Integer, ChallengeInfo> challenges = new HashMap<>();
        String sql = "SELECT uc.challenge_id, uc.start_date, c.Duration " +
                "FROM User_challenges uc " +
                "JOIN Challenge c ON uc.challenge_id = c.ChallengeID " +
                "WHERE uc.user_id = ?";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getCurrentUserID());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int challengeId = rs.getInt("challenge_id");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                int duration = rs.getInt("Duration");
                challenges.put(challengeId, new ChallengeInfo(startDate, duration));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return challenges;
    }

    private String getChallengeTitle(int challengeId) {
        String sql = "SELECT ChallengeTitle FROM Challenge WHERE ChallengeID = ?";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, challengeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("ChallengeTitle");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Challenge";
    }

    private boolean isChallengeCompletedOnDate(int challengeId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE user_id = ? AND challenge_id = ? AND completion_date = ? AND activity_type = 'challenge'";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getCurrentUserID());
            stmt.setInt(2, challengeId);
            stmt.setDate(3, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void checkInactiveChallenges() {
        String sql = "SELECT challenge_id, last_completed_date FROM User_challenges WHERE user_id = ?";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getCurrentUserID());
            ResultSet rs = stmt.executeQuery();
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                int challengeId = rs.getInt("challenge_id");
                LocalDate lastCompleted = rs.getDate("last_completed_date") != null ?
                        rs.getDate("last_completed_date").toLocalDate() : null;
                if (lastCompleted == null || today.minusDays(3).isAfter(lastCompleted)) {
                    removeChallenge(challengeId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeChallenge(int challengeId) {
        String sql = "DELETE FROM User_challenges WHERE user_id = ? AND challenge_id = ?";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getCurrentUserID());
            stmt.setInt(2, challengeId);
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Challenge Removed",
                    "Challenge " + getChallengeTitle(challengeId) + " removed due to 3 days of inactivity.");
            displayCalendar();
            updateSidePanel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getProgressTooltip(LocalDate date) {
        int userId = Session.getCurrentUserID();
        int exerciseCount = userDao.getExerciseCountForDate(userId, date);
        int challengeCount = getChallengeCountForDate(userId, date);
        int programCount = getProgramCountForDate(userId, date);

        StringBuilder tooltip = new StringBuilder();
        if (exerciseCount > 0) tooltip.append(exerciseCount).append(" workout").append(exerciseCount != 1 ? "s" : "").append("\n");
        if (challengeCount > 0) tooltip.append(challengeCount).append(" challenge").append(challengeCount != 1 ? "s" : "").append("\n");
        if (programCount > 0) tooltip.append(programCount).append(" program").append(programCount != 1 ? "s" : "");
        return tooltip.length() > 0 ? tooltip.toString().trim() : "No activity";
    }

    private int getChallengeCountForDate(int userId, LocalDate date) {
        String sql = "SELECT COUNT(DISTINCT challenge_id) FROM user_progress WHERE user_id = ? AND completion_date = ? AND activity_type = 'challenge'";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getProgramCountForDate(int userId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM user_progress WHERE user_id = ? AND completion_date = ? AND activity_type = 'program'";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void showProgressDetails(LocalDate date) {
        int userId = Session.getCurrentUserID();
        java.util.List<String> workouts = new java.util.ArrayList<>();
        java.util.List<String> challenges = new java.util.ArrayList<>();
        java.util.List<String> programs = new java.util.ArrayList<>();

        String sql = "SELECT activity_type, e.exerciseTitle, c.ChallengeTitle, p.ProgramTitle, up.challenge_id " +
                "FROM user_progress up " +
                "LEFT JOIN Exercise e ON up.exercise_id = e.ExerciseID " +
                "LEFT JOIN Challenge c ON up.challenge_id = c.ChallengeID " +
                "LEFT JOIN Program p ON up.program_id = p.ProgramID " +
                "WHERE up.user_id = ? AND up.completion_date = ?";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String activityType = rs.getString("activity_type");
                switch (activityType) {
                    case "exercise":
                        String exerciseTitle = rs.getString("exerciseTitle");
                        if (exerciseTitle != null) workouts.add(exerciseTitle);
                        break;
                    case "challenge":
                        String challengeTitle = rs.getString("ChallengeTitle");
                        if (challengeTitle != null) {
                            challenges.add(challengeTitle);
                            String exercisesSql = "SELECT e.exerciseTitle FROM Challenge_Exercise ce " +
                                    "JOIN Exercise e ON ce.ExerciseID = e.ExerciseID " +
                                    "WHERE ce.ChallengeID = ?";
                            try (PreparedStatement exStmt = conn.prepareStatement(exercisesSql)) {
                                exStmt.setInt(1, rs.getInt("challenge_id"));
                                ResultSet exRs = exStmt.executeQuery();
                                while (exRs.next()) {
                                    String exTitle = exRs.getString("exerciseTitle");
                                    if (exTitle != null) workouts.add(" - " + exTitle);
                                }
                            }
                        }
                        break;
                    case "program":
                        String programTitle = rs.getString("ProgramTitle");
                        if (programTitle != null) programs.add(programTitle + " - Cardio - Running - Core - Cycling");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        workoutDetails.setText(workouts.isEmpty() ? "No workouts completed" : String.join("\n", workouts));
        challengeDetails.setText(challenges.isEmpty() ? "No challenges completed" : String.join("\n", challenges));
        programDetails.setText(programs.isEmpty() ? "No programs completed" : String.join("\n", programs));
    }

    private void checkConsistencyBonus() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        int exerciseStreak = userDao.getExerciseStreak(currentUser.getId());
        if (exerciseStreak >= 7) {
            int bonusExp = 100;
            currentUser.setExp(currentUser.getExp() + bonusExp);
            userDao.updateUserExp(currentUser.getId(), currentUser.getExp());
            showAlert(Alert.AlertType.INFORMATION, "Exercise Streak Bonus", "Gained " + bonusExp + " EXP for a 7-day exercise streak!");
        }

        int challengeStreak = getChallengeStreak(currentUser.getId());
        if (challengeStreak >= 5) {
            int bonusExp = 150;
            currentUser.setExp(currentUser.getExp() + bonusExp);
            userDao.updateUserExp(currentUser.getId(), currentUser.getExp());
            showAlert(Alert.AlertType.INFORMATION, "Challenge Streak Bonus", "Gained " + bonusExp + " EXP for a 5-day challenge streak!");
        }
    }

    private int getChallengeStreak(int userId) {
        String sql = "SELECT completion_date FROM user_progress WHERE user_id = ? AND activity_type = 'challenge' " +
                "ORDER BY completion_date DESC";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            LocalDate today = LocalDate.now();
            int streak = 0;
            LocalDate lastDate = null;

            while (rs.next()) {
                LocalDate date = rs.getDate("completion_date").toLocalDate();
                if (lastDate == null) {
                    if (!date.equals(today)) break;
                    streak = 1;
                    lastDate = date;
                } else if (lastDate.minusDays(1).equals(date)) {
                    streak++;
                    lastDate = date;
                } else {
                    break;
                }
            }
            return streak;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void logChallengeCompletion(int challengeId) {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        LocalDate today = LocalDate.now();
        ChallengeDAO challengeDAO = new ChallengeDAO();
        Challenge challenge = challengeDAO.getChallengeById(challengeId);
        int bonusExp = challenge != null ? challenge.getDifficulty() * 100 : 100;

        boolean success = userDao.logProgress(currentUser.getId(), "challenge", challengeId, today, 0);
        if (success) {
            updateLastCompletedDate(currentUser.getId(), challengeId, today);
            currentUser.setExp(currentUser.getExp() + bonusExp);
            userDao.updateUserExp(currentUser.getId(), currentUser.getExp());
            showAlert(Alert.AlertType.INFORMATION, "Challenge Completed", "Gained " + bonusExp + " bonus EXP!");
            displayCalendar();
            checkConsistencyBonus();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to log challenge completion.");
        }
    }

    private void updateLastCompletedDate(int userId, int challengeId, LocalDate date) {
        String sql = "UPDATE User_challenges SET last_completed_date = ? WHERE user_id = ? AND challenge_id = ?";
        try (Connection conn = project.uprising.User.Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            stmt.setInt(2, userId);
            stmt.setInt(3, challengeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class ChallengeInfo {
    LocalDate startDate;
    int duration;

    ChallengeInfo(LocalDate startDate, int duration) {
        this.startDate = startDate;
        this.duration = duration;
    }
}