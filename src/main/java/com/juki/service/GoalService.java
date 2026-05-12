package com.juki.service;

import com.juki.controller.GoalController;
import com.juki.model.SelfCareGoal;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.time.LocalDate;
import java.util.List;

/**
 * GoalService acts as the "Shared Source of Truth" for the application.
 * It manages the goals state and streak calculations reactively.
 */
public class GoalService {
    private static GoalService instance;
    private final GoalController goalController;
    
    // Observable Map to hold goals per date for realtime sync
    private final ObservableMap<LocalDate, List<SelfCareGoal>> goalsCache = FXCollections.observableHashMap();
    private final IntegerProperty streakProperty = new SimpleIntegerProperty(0);

    private GoalService() {
        this.goalController = new GoalController();
        refreshAll();
    }

    public static GoalService getInstance() {
        if (instance == null) {
            instance = new GoalService();
        }
        return instance;
    }

    /**
     * Refresh the entire cache and streak from the database.
     */
    public void refreshAll() {
        // Clear cache and reload (simplified for this context)
        // In a larger app, we'd load only what's needed
        // For now, we'll refresh on demand or for specific dates
        updateStreak();
    }

    public ObservableMap<LocalDate, List<SelfCareGoal>> getGoalsCache() {
        return goalsCache;
    }

    public List<SelfCareGoal> getGoalsForDate(LocalDate date) {
        if (!goalsCache.containsKey(date)) {
            refreshDate(date);
        }
        return goalsCache.get(date);
    }

    public void refreshDate(LocalDate date) {
        List<SelfCareGoal> goals = goalController.getGoalsByDate(date);
        goalsCache.put(date, goals);
        updateStreak();
    }

    public void toggleGoalStatus(SelfCareGoal goal) {
        goalController.updateGoalStatus(goal.getId(), !goal.isCompleted());
        refreshDate(goal.getDate());
    }

    /**
     * Toggles all targets for a specific date as completed or uncompleted.
     */
    public void toggleAllForDate(LocalDate date) {
        List<SelfCareGoal> goals = getGoalsForDate(date);
        if (goals.isEmpty()) return;

        boolean anyUnfinished = goals.stream().anyMatch(g -> !g.isCompleted());
        for (SelfCareGoal g : goals) {
            goalController.updateGoalStatus(g.getId(), anyUnfinished);
        }
        refreshDate(date);
    }

    public void saveGoalsForDate(LocalDate date, List<SelfCareGoal> goals) {
        goalController.saveGoalsForDate(date, goals);
        refreshDate(date);
    }

    public void deleteAllForDate(LocalDate date) {
        goalController.deleteAllGoalsForDate(date);
        refreshDate(date);
    }

    public IntegerProperty streakProperty() {
        return streakProperty;
    }

    public int getStreak() {
        return streakProperty.get();
    }

    private void updateStreak() {
        streakProperty.set(goalController.calculateStreak(0)); // userId ignored for now
    }

    /**
     * Consistency Rule: A day is completed if it has targets and ALL targets are done.
     */
    public boolean isDayCompleted(LocalDate date) {
        List<SelfCareGoal> goals = getGoalsForDate(date);
        return !goals.isEmpty() && goals.stream().allMatch(SelfCareGoal::isCompleted);
    }
}