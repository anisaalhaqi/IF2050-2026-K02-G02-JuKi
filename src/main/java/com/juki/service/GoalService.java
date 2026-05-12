package com.juki.service;

import com.juki.controller.GoalController;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * GoalService optimized for performance and to prevent UI freezes.
 */
public class GoalService {
    private static GoalService instance;
    private final GoalController goalController;
    private int currentUserId = -1;
    
    private final ObservableMap<LocalDate, List<SelfCareGoal>> goalsCache = FXCollections.observableHashMap();
    private final IntegerProperty streakProperty = new SimpleIntegerProperty(0);
    private boolean isUpdating = false;

    private GoalService() {
        this.goalController = new GoalController();
    }

    public static GoalService getInstance() {
        if (instance == null) instance = new GoalService();
        return instance;
    }

    public void setCurrentUser(User user) {
        if (user != null && user.getId() != currentUserId) {
            this.currentUserId = user.getId();
            
            // Bulk loading to prevent multiple listener triggers
            isUpdating = true;
            try {
                goalsCache.clear();
                // Fetch all goals for the user at once if possible, or at least the relevant range
                // For now, let's load the last 30 days and the current month
                LocalDate start = LocalDate.now().minusDays(14);
                for (int i = 0; i <= 31; i++) {
                    LocalDate d = start.plusDays(i);
                    List<SelfCareGoal> goals = goalController.getGoalsByDate(d, currentUserId);
                    goalsCache.put(d, goals != null ? goals : new ArrayList<>());
                }
                updateStreak();
            } finally {
                isUpdating = false;
            }
        }
    }

    public ObservableMap<LocalDate, List<SelfCareGoal>> getGoalsCache() { return goalsCache; }

    public List<SelfCareGoal> getGoalsForDate(LocalDate date) {
        if (!goalsCache.containsKey(date)) {
            refreshDate(date);
        }
        return goalsCache.getOrDefault(date, new ArrayList<>());
    }

    public void refreshDate(LocalDate date) {
        if (currentUserId == -1) return;
        // Don't trigger if already in a bulk update
        boolean nested = isUpdating;
        if (!nested) isUpdating = true;
        try {
            List<SelfCareGoal> goals = goalController.getGoalsByDate(date, currentUserId);
            goalsCache.put(date, goals != null ? goals : new ArrayList<>());
            updateStreak();
        } finally {
            if (!nested) isUpdating = false;
        }
    }

    public void toggleGoalStatus(SelfCareGoal goal) {
        if (currentUserId == -1 || goal == null) return;
        goalController.updateGoalStatus(goal.getId(), !goal.isCompleted(), currentUserId);
        refreshDate(goal.getDate());
    }

    public void toggleAllForDate(LocalDate date) {
        if (currentUserId == -1) return;
        List<SelfCareGoal> goals = getGoalsForDate(date);
        if (goals.isEmpty()) return;
        boolean anyUnfinished = goals.stream().anyMatch(g -> !g.isCompleted());
        for (SelfCareGoal g : goals) {
            goalController.updateGoalStatus(g.getId(), anyUnfinished, currentUserId);
        }
        refreshDate(date);
    }

    public void saveGoalsForDate(LocalDate date, List<SelfCareGoal> goals) {
        if (currentUserId == -1) return;
        goalController.saveGoalsForDate(date, goals, currentUserId);
        refreshDate(date);
    }

    public void deleteAllForDate(LocalDate date) {
        if (currentUserId == -1) return;
        goalController.deleteAllGoalsForDate(date, currentUserId);
        refreshDate(date);
    }

    public IntegerProperty streakProperty() { return streakProperty; }
    public int getStreak() { return streakProperty.get(); }

    public void updateStreak() {
        if (currentUserId != -1) {
            int val = goalController.calculateStreak(currentUserId);
            if (streakProperty.get() != val) streakProperty.set(val);
        }
    }

    public boolean isDayCompleted(LocalDate date) {
        List<SelfCareGoal> goals = getGoalsForDate(date);
        return !goals.isEmpty() && goals.stream().allMatch(SelfCareGoal::isCompleted);
    }
}