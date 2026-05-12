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

public class GoalService {
    private static GoalService instance;
    private final GoalController goalController;
    private int currentUserId = -1;
    
    private final ObservableMap<LocalDate, List<SelfCareGoal>> goalsCache = FXCollections.observableHashMap();
    private final IntegerProperty streakProperty = new SimpleIntegerProperty(0);

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
            goalsCache.clear();
            updateStreak();
        }
    }

    public ObservableMap<LocalDate, List<SelfCareGoal>> getGoalsCache() { return goalsCache; }

    public List<SelfCareGoal> getGoalsForDate(LocalDate date) {
        if (!goalsCache.containsKey(date)) {
            List<SelfCareGoal> goals = goalController.getGoalsByDate(date, currentUserId);
            goalsCache.put(date, goals != null ? goals : new ArrayList<>());
        }
        return goalsCache.getOrDefault(date, new ArrayList<>());
    }

    public void toggleGoalStatus(SelfCareGoal goal) {
        if (currentUserId == -1 || goal == null) return;
        goalController.updateGoalStatus(goal.getId(), !goal.isCompleted(), currentUserId);
        refreshDate(goal.getDate());
    }

    public void toggleAllForDate(LocalDate date) {
        List<SelfCareGoal> goals = getGoalsForDate(date);
        if (goals.isEmpty()) return;
        boolean anyUnfinished = goals.stream().anyMatch(g -> !g.isCompleted());
        for (SelfCareGoal g : goals) {
            goalController.updateGoalStatus(g.getId(), anyUnfinished, currentUserId);
        }
        refreshDate(date);
    }

    public void refreshDate(LocalDate date) {
        List<SelfCareGoal> goals = goalController.getGoalsByDate(date, currentUserId);
        goalsCache.put(date, goals != null ? goals : new ArrayList<>());
        updateStreak();
    }

    public void saveGoalsForDate(LocalDate date, List<SelfCareGoal> goals) {
        goalController.saveGoalsForDate(date, goals, currentUserId);
        refreshDate(date);
    }

    public void deleteAllForDate(LocalDate date) {
        goalController.deleteAllGoalsForDate(date, currentUserId);
        refreshDate(date);
    }

    public IntegerProperty streakProperty() { return streakProperty; }
    public int getStreak() { return streakProperty.get(); }

    public void updateStreak() {
        if (currentUserId != -1) {
            streakProperty.set(goalController.calculateStreak(currentUserId));
        }
    }

    public boolean isDayCompleted(LocalDate date) {
        List<SelfCareGoal> goals = getGoalsForDate(date);
        return !goals.isEmpty() && goals.stream().allMatch(SelfCareGoal::isCompleted);
    }
}