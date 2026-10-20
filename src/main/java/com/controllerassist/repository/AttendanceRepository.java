package com.controllerassist.repository;

import com.controllerassist.model.AttendanceRecord;
import com.controllerassist.model.AttendanceStatus;
import com.controllerassist.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeAndAttendanceDate(Employee employee, LocalDate attendanceDate);

    List<AttendanceRecord> findByAttendanceDate(LocalDate attendanceDate);

    List<AttendanceRecord> findByAttendanceDateOrderByCheckInTimeDesc(LocalDate attendanceDate);

    List<AttendanceRecord> findByAttendanceDateBetweenOrderByAttendanceDateDescCheckInTimeDesc(
            LocalDate startDate, LocalDate endDate);

    List<AttendanceRecord> findByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);

    @Query("SELECT a FROM AttendanceRecord a WHERE " +
           "(:employeeId IS NULL OR a.employee.id = :employeeId) AND " +
           "(:startDate IS NULL OR a.attendanceDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.attendanceDate <= :endDate) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.attendanceDate DESC, a.checkInTime DESC")
    List<AttendanceRecord> filterAttendances(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status);

    long countByAttendanceDate(LocalDate date);

    long countByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);
}
