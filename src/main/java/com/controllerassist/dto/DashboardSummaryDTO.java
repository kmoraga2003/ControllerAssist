package com.controllerassist.dto;

import java.time.LocalDateTime;

public class DashboardSummaryDTO {

    private long totalEmployees;
    private long presentToday;
    private long lateToday;
    private long onTimeToday;
    private long absentToday;
    private LocalDateTime serverTime;

    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(long totalEmployees, long presentToday, long lateToday, long onTimeToday, long absentToday, LocalDateTime serverTime) {
        this.totalEmployees = totalEmployees;
        this.presentToday = presentToday;
        this.lateToday = lateToday;
        this.onTimeToday = onTimeToday;
        this.absentToday = absentToday;
        this.serverTime = serverTime;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getPresentToday() {
        return presentToday;
    }

    public void setPresentToday(long presentToday) {
        this.presentToday = presentToday;
    }

    public long getLateToday() {
        return lateToday;
    }

    public void setLateToday(long lateToday) {
        this.lateToday = lateToday;
    }

    public long getOnTimeToday() {
        return onTimeToday;
    }

    public void setOnTimeToday(long onTimeToday) {
        this.onTimeToday = onTimeToday;
    }

    public long getAbsentToday() {
        return absentToday;
    }

    public void setAbsentToday(long absentToday) {
        this.absentToday = absentToday;
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }
}
