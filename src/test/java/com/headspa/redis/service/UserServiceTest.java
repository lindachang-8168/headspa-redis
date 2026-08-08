package com.headspa.redis.service;

import com.headspa.redis.model.User;
import com.headspa.redis.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Liam");
        existingUser.setPhone("0910234567");
        existingUser.setEmail("liam@yahoo.com.tw");
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Liam");
    }

    @Test
    void findById_whenExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        Optional<User> result = userService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("liam@yahoo.com.tw");
    }

    @Test
    void findById_whenNotExists_shouldReturnEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldCallRepositorySave() {
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.save(new User());

        assertThat(result).isEqualTo(existingUser);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_whenUserExists_shouldUpdateFieldsAndSave() {
        User updatedInfo = new User();
        updatedInfo.setName("Liam Chen");
        updatedInfo.setPhone("0910234567");
        updatedInfo.setEmail("liam.chen@yahoo.com.tw");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(1L, updatedInfo);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Liam Chen");
        assertThat(result.getEmail()).isEqualTo("liam.chen@yahoo.com.tw");
    }

    @Test
    void update_whenUserNotExists_shouldReturnNull() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userService.update(999L, new User());

        assertThat(result).isNull();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        userService.deleteById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}
