package com.headspa.redis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.headspa.redis.model.User;
import com.headspa.redis.service.UserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Liam");
        user.setPhone("0910234567");
        user.setEmail("liam@yahoo.com.tw");
        return user;
    }

    @Test
    void getAll_shouldReturnUserList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Liam"));
    }

    @Test
    void getById_whenExists_shouldReturnUser() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(sampleUser()));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("liam@yahoo.com.tw"));
    }

    @Test
    void getById_whenNotExists_shouldReturn404() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        User newUser = new User();
        newUser.setName("Sophia");
        newUser.setPhone("0930234567");
        newUser.setEmail("sophia@yahoo.com.tw");

        User saved = sampleUser();
        saved.setName("Sophia");
        saved.setEmail("sophia@yahoo.com.tw");

        when(userService.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sophia"));
    }

    @Test
    void update_whenExists_shouldReturnUpdatedUser() throws Exception {
        User updateRequest = new User();
        updateRequest.setName("Liam Chen");
        updateRequest.setPhone("0910234567");
        updateRequest.setEmail("liam.chen@yahoo.com.tw");

        User updated = sampleUser();
        updated.setName("Liam Chen");
        updated.setEmail("liam.chen@yahoo.com.tw");

        when(userService.update(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Liam Chen"));
    }

    @Test
    void update_whenNotExists_shouldReturn404() throws Exception {
        when(userService.update(eq(999L), any(User.class))).thenReturn(null);

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(userService).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteById(1L);
    }
}
