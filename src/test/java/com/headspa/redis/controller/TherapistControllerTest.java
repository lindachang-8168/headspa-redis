package com.headspa.redis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.headspa.redis.model.Therapist;
import com.headspa.redis.service.TherapistService;
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

@WebMvcTest(TherapistController.class)
class TherapistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TherapistService therapistService;

    private Therapist sampleTherapist() {
        Therapist therapist = new Therapist();
        therapist.setId(1L);
        therapist.setName("Ethan");
        therapist.setPhone("0920234567");
        therapist.setSpecialty("頭部經絡按摩");
        return therapist;
    }

    @Test
    void getAll_shouldReturnTherapistList() throws Exception {
        when(therapistService.findAll()).thenReturn(List.of(sampleTherapist()));

        mockMvc.perform(get("/api/therapists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ethan"));
    }

    @Test
    void getById_whenExists_shouldReturnTherapist() throws Exception {
        when(therapistService.findById(1L)).thenReturn(Optional.of(sampleTherapist()));

        mockMvc.perform(get("/api/therapists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("頭部經絡按摩"));
    }

    @Test
    void getById_whenNotExists_shouldReturn404() throws Exception {
        when(therapistService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/therapists/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturnCreatedTherapist() throws Exception {
        Therapist newTherapist = new Therapist();
        newTherapist.setName("Luna");
        newTherapist.setPhone("0950234567");
        newTherapist.setSpecialty("肩頸舒壓");

        Therapist saved = sampleTherapist();
        saved.setName("Luna");
        saved.setSpecialty("肩頸舒壓");

        when(therapistService.save(any(Therapist.class))).thenReturn(saved);

        mockMvc.perform(post("/api/therapists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTherapist)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luna"));
    }

    @Test
    void update_whenExists_shouldReturnUpdatedTherapist() throws Exception {
        Therapist updateRequest = new Therapist();
        updateRequest.setName("Ethan Wu");
        updateRequest.setPhone("0920234567");
        updateRequest.setSpecialty("頭部經絡按摩 / 熱石按摩");

        Therapist updated = sampleTherapist();
        updated.setName("Ethan Wu");
        updated.setSpecialty("頭部經絡按摩 / 熱石按摩");

        when(therapistService.update(eq(1L), any(Therapist.class))).thenReturn(updated);

        mockMvc.perform(put("/api/therapists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ethan Wu"));
    }

    @Test
    void update_whenNotExists_shouldReturn404() throws Exception {
        when(therapistService.update(eq(999L), any(Therapist.class))).thenReturn(null);

        mockMvc.perform(put("/api/therapists/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTherapist())))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(therapistService).deleteById(1L);

        mockMvc.perform(delete("/api/therapists/1"))
                .andExpect(status().isNoContent());

        verify(therapistService, times(1)).deleteById(1L);
    }
}
