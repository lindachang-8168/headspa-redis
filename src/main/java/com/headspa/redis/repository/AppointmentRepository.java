package com.headspa.redis.repository;

import com.headspa.redis.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserId(Long userId);

    List<Appointment> findByTherapistId(Long therapistId);

    // 新增預約時：同芳療師、同日期、同時間，是否已有「非取消」的預約
    boolean existsByTherapistIdAndDateAndTimeAndStatusNot(
            Long therapistId, String date, String time, String status);

    // 修改預約時：同上，但要排除這筆預約自己
    boolean existsByTherapistIdAndDateAndTimeAndStatusNotAndIdNot(
            Long therapistId, String date, String time, String status, Long id);
}
