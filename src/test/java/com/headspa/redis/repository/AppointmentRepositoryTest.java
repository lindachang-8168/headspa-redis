package com.headspa.redis.repository;

import com.headspa.redis.model.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Appointment newAppointment(String date, String time, String status, Long userId, Long therapistId) {
        Appointment a = new Appointment();
        a.setDate(date);
        a.setTime(time);
        a.setStatus(status);
        a.setUserId(userId);
        a.setTherapistId(therapistId);
        return a;
    }

    @Test
    void save_shouldPersistAppointmentAndGenerateId() {
        Appointment saved = appointmentRepository.save(
                newAppointment("2026-08-01", "10:00", "BOOKED", 1L, 1L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("BOOKED");
    }

    @Test
    void findByUserId_shouldReturnOnlyMatchingAppointments() {
        appointmentRepository.save(newAppointment("2026-08-01", "10:00", "BOOKED", 1L, 1L));
        appointmentRepository.save(newAppointment("2026-08-02", "11:00", "BOOKED", 1L, 2L));
        appointmentRepository.save(newAppointment("2026-08-03", "12:00", "BOOKED", 2L, 1L));

        List<Appointment> result = appointmentRepository.findByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getUserId().equals(1L));
    }

    @Test
    void findByTherapistId_shouldReturnOnlyMatchingAppointments() {
        appointmentRepository.save(newAppointment("2026-08-01", "10:00", "BOOKED", 1L, 5L));
        appointmentRepository.save(newAppointment("2026-08-02", "11:00", "BOOKED", 2L, 5L));
        appointmentRepository.save(newAppointment("2026-08-03", "12:00", "BOOKED", 3L, 9L));

        List<Appointment> result = appointmentRepository.findByTherapistId(5L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getTherapistId().equals(5L));
    }

    @Test
    void deleteById_shouldRemoveAppointment() {
        Appointment saved = appointmentRepository.save(
                newAppointment("2026-08-01", "10:00", "BOOKED", 1L, 1L));

        appointmentRepository.deleteById(saved.getId());

        assertThat(appointmentRepository.findById(saved.getId())).isEmpty();
    }
}
