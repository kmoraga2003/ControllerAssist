package com.controllerassist.dto;

import com.controllerassist.model.AttendanceRecord;
import com.controllerassist.model.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceResponseDTO {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String department;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    private String statusDescription;
    private String notes;
    private String message;

    public AttendanceResponseDTO() {
    }

    public static AttendanceResponseDTO fromEntity(AttendanceRecord record) {
        AttendanceResponseDTO dto = new AttendanceResponseDTO();
        dto.setId(record.getId());
        if (record.getEmployee() != null) {
            dto.setEmployeeId(record.getEmployee().getId());
            dto.setEmployeeCode(record.getEmployee().getCode());
            dto.setEmployeeName(record.getEmployee().getFullName());
            dto.setDepartment(record.getEmployee().getDepartment());
        }
        dto.setAttendanceDate(record.getAttendanceDate());
        dto.setCheckInTime(record.getCheckInTime());
        dto.setCheckOutTime(record.getCheckOutTime());
        dto.setStatus(record.getStatus());
        dto.setStatusDescription(record.getStatus() != null ? record.getStatus().getDisplayName() : "");
        dto.setNotes(record.getNotes());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
