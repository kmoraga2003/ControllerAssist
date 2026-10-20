package com.controllerassist.dto;

import com.controllerassist.model.Employee;
import com.controllerassist.model.Role;

public class LoginResponseDTO {

    private Long id;
    private String code;
    private String fullName;
    private String email;
    private String department;
    private String position;
    private Role role;
    private String message;

    public LoginResponseDTO() {
    }

    public static LoginResponseDTO fromEmployee(Employee employee, String message) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setId(employee.getId());
        dto.setCode(employee.getCode());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setDepartment(employee.getDepartment());
        dto.setPosition(employee.getPosition());
        dto.setRole(employee.getRole());
        dto.setMessage(message);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
