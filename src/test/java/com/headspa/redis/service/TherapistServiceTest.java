package com.headspa.redis.service;

import com.headspa.redis.model.Therapist;
import com.headspa.redis.repository.TherapistRepository;
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
class TherapistServiceTest {

    @Mock
    private TherapistRepository therapistRepository;

    @InjectMocks
    private TherapistService therapistService;

    private Therapist existingTherapist;

    @BeforeEach
    void setUp() {
        existingTherapist = new Therapist();
        existingTherapist.setId(1L);
        existingTherapist.setName("Ethan");
        existingTherapist.setPhone("0920234567");
        existingTherapist.setSpecialty("頭部經絡按摩");
    }

    @Test
    void findAll_shouldReturnAllTherapists() {
        when(therapistRepository.findAll()).thenReturn(List.of(existingTherapist));

        List<Therapist> result = therapistService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Ethan");
    }

    @Test
    void findById_whenExists_shouldReturnTherapist() {
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(existingTherapist));

        Optional<Therapist> result = therapistService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSpecialty()).isEqualTo("頭部經絡按摩");
    }

    @Test
    void findById_whenNotExists_shouldReturnEmpty() {
        when(therapistRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Therapist> result = therapistService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldCallRepositorySave() {
        when(therapistRepository.save(any(Therapist.class))).thenReturn(existingTherapist);

        Therapist result = therapistService.save(new Therapist());

        assertThat(result).isEqualTo(existingTherapist);
        verify(therapistRepository, times(1)).save(any(Therapist.class));
    }

    @Test
    void update_whenTherapistExists_shouldUpdateFieldsAndSave() {
        Therapist updatedInfo = new Therapist();
        updatedInfo.setName("Ethan Wu");
        updatedInfo.setPhone("0920234567");
        updatedInfo.setSpecialty("頭部經絡按摩 / 熱石按摩");

        when(therapistRepository.findById(1L)).thenReturn(Optional.of(existingTherapist));
        when(therapistRepository.save(any(Therapist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Therapist result = therapistService.update(1L, updatedInfo);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Ethan Wu");
        assertThat(result.getSpecialty()).isEqualTo("頭部經絡按摩 / 熱石按摩");
    }

    @Test
    void update_whenTherapistNotExists_shouldReturnNull() {
        when(therapistRepository.findById(999L)).thenReturn(Optional.empty());

        Therapist result = therapistService.update(999L, new Therapist());

        assertThat(result).isNull();
        verify(therapistRepository, never()).save(any(Therapist.class));
    }

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        therapistService.deleteById(1L);

        verify(therapistRepository, times(1)).deleteById(1L);
    }
}
