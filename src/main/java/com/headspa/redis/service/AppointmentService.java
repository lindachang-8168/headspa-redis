package com.headspa.redis.service;

import com.headspa.redis.model.Appointment;
import com.headspa.redis.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private static final String CACHE_PREFIX = "therapist:appointments:";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> findByUserId(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }

    @SuppressWarnings("unchecked")
    public List<Appointment> findByTherapistId(Long therapistId) {
        String cacheKey = CACHE_PREFIX + therapistId;

        // 先查 Redis 快取；Redis 連不上就直接跳過快取，改查資料庫
        try {
            List<Appointment> cached = (List<Appointment>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis 目前連不上，本次查詢跳過快取，直接查資料庫: {}", e.getMessage());
        }

        List<Appointment> result = appointmentRepository.findByTherapistId(therapistId);

        try {
            redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis 目前連不上，本次查詢結果不寫入快取: {}", e.getMessage());
        }

        return result;
    }

    public Appointment save(Appointment appointment) {
        checkTimeSlotAvailable(appointment, null);
        Appointment saved = appointmentRepository.save(appointment);
        evictTherapistCache(saved.getTherapistId());
        return saved;
    }

    public Appointment update(Long id, Appointment updated) {
        return appointmentRepository.findById(id).map(appointment -> {
            checkTimeSlotAvailable(updated, id);
            appointment.setDate(updated.getDate());
            appointment.setTime(updated.getTime());
            appointment.setStatus(updated.getStatus());
            appointment.setUserId(updated.getUserId());
            appointment.setTherapistId(updated.getTherapistId());
            Appointment saved = appointmentRepository.save(appointment);
            evictTherapistCache(saved.getTherapistId());
            return saved;
        }).orElse(null);
    }

    public void deleteById(Long id) {
        appointmentRepository.findById(id).ifPresent(appointment -> {
            appointmentRepository.deleteById(id);
            evictTherapistCache(appointment.getTherapistId());
        });
    }

    /**
     * 同一位芳療師、同一天、同一時段只能有一筆有效預約。
     * - 已取消（CANCELLED）的預約不佔用時段，取消後別人可以再預約
     * - 把預約改成取消時不需要檢查
     * - 修改預約時排除自己（excludeId），例如只改狀態不會跟自己衝突
     */
    private void checkTimeSlotAvailable(Appointment appointment, Long excludeId) {
        if (appointment.getTherapistId() == null || appointment.getDate() == null
                || appointment.getTime() == null || STATUS_CANCELLED.equals(appointment.getStatus())) {
            return;
        }
        boolean taken = (excludeId == null)
                ? appointmentRepository.existsByTherapistIdAndDateAndTimeAndStatusNot(
                        appointment.getTherapistId(), appointment.getDate(), appointment.getTime(), STATUS_CANCELLED)
                : appointmentRepository.existsByTherapistIdAndDateAndTimeAndStatusNotAndIdNot(
                        appointment.getTherapistId(), appointment.getDate(), appointment.getTime(), STATUS_CANCELLED, excludeId);
        if (taken) {
            throw new TimeSlotConflictException(appointment.getDate(), appointment.getTime());
        }
    }

    private void evictTherapistCache(Long therapistId) {
        if (therapistId == null) {
            return;
        }
        try {
            redisTemplate.delete(CACHE_PREFIX + therapistId);
        } catch (RedisConnectionFailureException e) {
            // Redis 沒開也沒關係，MySQL 那筆資料已經存好了，快取之後補上就好
            log.warn("Redis 目前連不上，跳過清除快取這個步驟: {}", e.getMessage());
        }
    }
}
