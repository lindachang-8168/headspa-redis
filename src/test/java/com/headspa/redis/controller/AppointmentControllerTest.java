package com.headspa.redis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.headspa.redis.model.Appointment;
import com.headspa.redis.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private Appointment sampleAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDate("2026-08-01");
        appointment.setTime("10:00");
        appointment.setStatus("BOOKED");
        appointment.setUserId(1L);
        appointment.setTherapistId(5L);
        return appointment;
    }

    @Test
    void getAll_shouldReturnAppointmentList() throws Exception {
        when(appointmentService.findAll()).thenReturn(List.of(sampleAppointment()));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("BOOKED"));
    }

    @Test
    void getById_whenExists_shouldReturnAppointment() throws Exception {
        when(appointmentService.findById(1L)).thenReturn(Optional.of(sampleAppointment()));

        mockMvc.perform(get("/api/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.therapistId").value(5));
    }

    @Test
    void getById_whenNotExists_shouldReturn404() throws Exception {
        when(appointmentService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/appointments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUser_shouldReturnMatchingAppointments() throws Exception {
        when(appointmentService.findByUserId(1L)).thenReturn(List.of(sampleAppointment()));

        mockMvc.perform(get("/api/appointments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void getByTherapist_shouldReturnMatchingAppointments() throws Exception {
        when(appointmentService.findByTherapistId(5L)).thenReturn(List.of(sampleAppointment()));

        mockMvc.perform(get("/api/appointments/therapist/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].therapistId").value(5));
    }

    @Test
    void create_shouldReturnCreatedAppointment() throws Exception {
        Appointment newAppointment = new Appointment();
        newAppointment.setDate("2026-08-02");
        newAppointment.setTime("14:00");
        newAppointment.setStatus("BOOKED");
        newAppointment.setUserId(2L);
        newAppointment.setTherapistId(5L);

        Appointment saved = sampleAppointment();
        saved.setDate("2026-08-02");
        saved.setTime("14:00");
        saved.setUserId(2L);

        when(appointmentService.save(any(Appointment.class))).thenReturn(saved);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAppointment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-08-02"));
    }

    @Test
    void update_whenExists_shouldReturnUpdatedAppointment() throws Exception {
        Appointment updateRequest = new Appointment();
        updateRequest.setDate("2026-08-01");
        updateRequest.setTime("10:00");
        updateRequest.setStatus("FINISHED");
        updateRequest.setUserId(1L);
        updateRequest.setTherapistId(5L);

        Appointment updated = sampleAppointment();
        updated.setStatus("FINISHED");

        when(appointmentService.update(eq(1L), any(Appointment.class))).thenReturn(updated);

        mockMvc.perform(put("/api/appointments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    void update_whenNotExists_shouldReturn404() throws Exception {
        when(appointmentService.update(eq(999L), any(Appointment.class))).thenReturn(null);

        mockMvc.perform(put("/api/appointments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleAppointment())))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(appointmentService).deleteById(1L);

        mockMvc.perform(delete("/api/appointments/1"))
                .andExpect(status().isNoContent());

        verify(appointmentService, times(1)).deleteById(1L);
    }
}
