package com.headspa.redis.service;

import com.headspa.redis.model.Appointment;
import com.headspa.redis.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment existingAppointment;

    @BeforeEach
    void setUp() {
        existingAppointment = new Appointment();
        existingAppointment.setId(1L);
        existingAppointment.setDate("2026-08-01");
        existingAppointment.setTime("10:00");
        existingAppointment.setStatus("BOOKED");
        existingAppointment.setUserId(1L);
        existingAppointment.setTherapistId(5L);
    }

    @Test
    void findAll_shouldReturnAllAppointments() {
        when(appointmentRepository.findAll()).thenReturn(List.of(existingAppointment));

        List<Appointment> result = appointmentService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUserId_shouldDelegateToRepository() {
        when(appointmentRepository.findByUserId(1L)).thenReturn(List.of(existingAppointment));

        List<Appointment> result = appointmentService.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void findByTherapistId_whenCacheHit_shouldReturnCachedResultWithoutHittingDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("therapist:appointments:5")).thenReturn(List.of(existingAppointment));

        List<Appointment> result = appointmentService.findByTherapistId(5L);

        assertThat(result).hasSize(1);
        verify(appointmentRepository, never()).findByTherapistId(anyLong());
    }

    @Test
    void findByTherapistId_whenCacheMiss_shouldQueryDbAndWriteCache() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("therapist:appointments:5")).thenReturn(null);
        when(appointmentRepository.findByTherapistId(5L)).thenReturn(List.of(existingAppointment));

        List<Appointment> result = appointmentService.findByTherapistId(5L);

        assertThat(result).hasSize(1);
        verify(appointmentRepository, times(1)).findByTherapistId(5L);
        verify(valueOperations, times(1)).set(eq("therapist:appointments:5"), any(), any());
    }

    @Test
    void findByTherapistId_whenRedisDown_shouldFallBackToDbWithoutThrowing() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RedisConnectionFailureException("connection refused"));
        when(appointmentRepository.findByTherapistId(5L)).thenReturn(List.of(existingAppointment));

        List<Appointment> result = appointmentService.findByTherapistId(5L);

        // Redis 連不上時不應該拋出例外，而是直接改查資料庫
        assertThat(result).hasSize(1);
        verify(appointmentRepository, times(1)).findByTherapistId(5L);
    }

    @Test
    void save_shouldPersistAndEvictTherapistCache() {
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(existingAppointment);

        Appointment result = appointmentService.save(new Appointment());

        assertThat(result).isEqualTo(existingAppointment);
        verify(redisTemplate, times(1)).delete("therapist:appointments:5");
    }

    @Test
    void save_whenRedisDown_shouldStillReturnSavedAppointment() {
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(existingAppointment);
        doThrow(new RedisConnectionFailureException("connection refused"))
                .when(redisTemplate).delete(anyString());

        Appointment result = appointmentService.save(new Appointment());

        // Redis 清快取失敗，不應該影響已經存進 MySQL 的結果
        assertThat(result).isEqualTo(existingAppointment);
    }

    @Test
    void update_whenAppointmentExists_shouldUpdateFieldsAndEvictCache() {
        Appointment updatedInfo = new Appointment();
        updatedInfo.setDate("2026-08-01");
        updatedInfo.setTime("10:00");
        updatedInfo.setStatus("FINISHED");
        updatedInfo.setUserId(1L);
        updatedInfo.setTherapistId(5L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.update(1L, updatedInfo);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("FINISHED");
        verify(redisTemplate, times(1)).delete("therapist:appointments:5");
    }

    @Test
    void update_whenAppointmentNotExists_shouldReturnNull() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        Appointment result = appointmentService.update(999L, new Appointment());

        assertThat(result).isNull();
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void deleteById_shouldDeleteAndEvictCache() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existingAppointment));

        appointmentService.deleteById(1L);

        verify(appointmentRepository, times(1)).deleteById(1L);
        verify(redisTemplate, times(1)).delete("therapist:appointments:5");
    }

    @Test
    void deleteById_whenAppointmentNotExists_shouldNotCallDeleteOrEvict() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        appointmentService.deleteById(999L);

        verify(appointmentRepository, never()).deleteById(anyLong());
        verify(redisTemplate, never()).delete(anyString());
    }
}
