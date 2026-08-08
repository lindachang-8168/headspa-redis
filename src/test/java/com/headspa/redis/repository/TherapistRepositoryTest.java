package com.headspa.redis.repository;

import com.headspa.redis.model.Therapist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TherapistRepositoryTest {

    @Autowired
    private TherapistRepository therapistRepository;

    @Test
    void save_shouldPersistTherapistAndGenerateId() {
        Therapist therapist = new Therapist();
        therapist.setName("Ethan");
        therapist.setPhone("0920234567");
        therapist.setSpecialty("頭部經絡按摩");

        Therapist saved = therapistRepository.save(therapist);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSpecialty()).isEqualTo("頭部經絡按摩");
    }

    @Test
    void findById_shouldReturnSavedTherapist() {
        Therapist therapist = new Therapist();
        therapist.setName("Luna");
        therapist.setPhone("0950234567");
        therapist.setSpecialty("肩頸舒壓");
        Therapist saved = therapistRepository.save(therapist);

        Optional<Therapist> found = therapistRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Luna");
    }

    @Test
    void findAll_shouldReturnAllSavedTherapists() {
        Therapist t1 = new Therapist();
        t1.setName("A");
        t1.setPhone("0911111111");
        t1.setSpecialty("Specialty A");

        Therapist t2 = new Therapist();
        t2.setName("B");
        t2.setPhone("0922222222");
        t2.setSpecialty("Specialty B");

        therapistRepository.save(t1);
        therapistRepository.save(t2);

        List<Therapist> all = therapistRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveTherapist() {
        Therapist therapist = new Therapist();
        therapist.setName("ToDelete");
        therapist.setPhone("0900000000");
        therapist.setSpecialty("Temp");
        Therapist saved = therapistRepository.save(therapist);

        therapistRepository.deleteById(saved.getId());

        assertThat(therapistRepository.findById(saved.getId())).isEmpty();
    }
}
