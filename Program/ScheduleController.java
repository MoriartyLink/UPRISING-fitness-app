package project.uprising.Program;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import project.uprising.Program.Exercise;
import project.uprising.Program.Program;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ScheduleController {
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;

    private int programId;
    private Program program;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate currentMonthDate;
    private ProgramDAO programDAO = new ProgramDAO();

    @FXML
    private void initialize() {
        // Set column constraints for 7 days
        for (int i = 0; i < 7; i++) {
            calendarGrid.getColumnConstraints().add(new javafx.scene.layout.ColumnConstraints() {{
                setPercentWidth(100.0 / 7);
            }});
        }
        // Set row constraints for 7 rows: 1 for headers + 6 for days
        for (int i = 0; i < 7; i++) {
            calendarGrid.getRowConstraints().add(new javafx.scene.layout.RowConstraints() {{
                setPercentHeight(100.0 / 7);
            }});
        }
        // Add day of week headers
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label header = new Label(daysOfWeek[i]);
            header.getStyleClass().add("header-label");
            calendarGrid.add(header, i, 0);
        }
    }

    public void setProgramId(int programId) {
        this.programId = programId;
        loadProgram();
    }

    private void loadProgram() {
        program = programDAO.getProgramById(programId);
        startDate = LocalDate.now();
        endDate = startDate.plusDays(program.getDuration() - 1);
        currentMonthDate = startDate.withDayOfMonth(1);
        displayCalendar();
    }

    private void displayCalendar() {
        // Clear previous days
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);

        LocalDate firstDayOfMonth = currentMonthDate.withDayOfMonth(1);
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0=Sun, 1=Mon, ..., 6=Sat
        LocalDate gridStartDate = firstDayOfMonth.minusDays(dayOfWeekValue);

        for (int i = 0; i < 42; i++) {
            LocalDate date = gridStartDate.plusDays(i);
            int dayOfMonth = date.getDayOfMonth();
            boolean isCurrentMonth = (date.getMonth() == currentMonthDate.getMonth());

            Label dayLabel = new Label(String.valueOf(dayOfMonth));
            dayLabel.getStyleClass().add("day-label");
            dayLabel.setAlignment(Pos.CENTER);

            StackPane cellPane = new StackPane();
            cellPane.getStyleClass().add("calendar-cell");
            if (!isCurrentMonth) {
                cellPane.getStyleClass().add("inactive-day");
            }
            if (date.isEqual(LocalDate.now())) {
                cellPane.getStyleClass().add("current-day");
            }

            if (date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))) {
                long daysSinceStart = ChronoUnit.DAYS.between(startDate, date);
                if (daysSinceStart >= 0 && daysSinceStart < program.getDuration()) {
                    String dayString = "Day " + (daysSinceStart + 1);
                    List<Exercise> exercises = programDAO.getExercisesForProgramDay(programId, dayString);
                    if (!exercises.isEmpty()) {
                        cellPane.getStyleClass().add("exercise-day");
                        StringBuilder tooltipText = new StringBuilder(dayString + ":\n");
                        for (Exercise ex : exercises) {
                            tooltipText.append("- ").append(ex.getExerciseTitle()).append("\n");
                        }
                        Tooltip tooltip = new Tooltip(tooltipText.toString());
                        Tooltip.install(cellPane, tooltip);
                    }
                }
            }

            cellPane.getChildren().add(dayLabel);
            int row = (i / 7) + 1; // Start from row 1, since row 0 is headers
            int col = i % 7;
            calendarGrid.add(cellPane, col, row);
        }

        monthLabel.setText(currentMonthDate.getMonth() + " " + currentMonthDate.getYear());
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
}