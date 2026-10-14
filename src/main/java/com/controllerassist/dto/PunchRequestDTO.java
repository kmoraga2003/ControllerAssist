package com.controllerassist.dto;

import com.controllerassist.model.PunchType;
import jakarta.validation.constraints.NotBlank;

public class PunchRequestDTO {

    @NotBlank(message = "El código de empleado es obligatorio")
    private String employeeCode;

    // Opcional: Si se omite, el sistema determina automáticamente si es entrada o salida
    private PunchType type;

    private String notes;

    public PunchRequestDTO() {
    }

    public PunchRequestDTO(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public PunchRequestDTO(String employeeCode, PunchType type, String notes) {
        this.employeeCode = employeeCode;
        this.type = type;
        this.notes = notes;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public PunchType getType() {
        return type;
    }

    public void setType(PunchType type) {
        this.type = type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
