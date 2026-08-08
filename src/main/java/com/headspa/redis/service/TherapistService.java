package com.headspa.redis.service;

import com.headspa.redis.model.Therapist;
import com.headspa.redis.repository.TherapistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TherapistService {

    @Autowired
    private TherapistRepository therapistRepository;

    public List<Therapist> findAll() {
        return therapistRepository.findAll();
    }

    public Optional<Therapist> findById(Long id) {
        return therapistRepository.findById(id);
    }

    public Therapist save(Therapist therapist) {
        return therapistRepository.save(therapist);
    }

    public Therapist update(Long id, Therapist updated) {
        return therapistRepository.findById(id).map(therapist -> {
            therapist.setName(updated.getName());
            therapist.setPhone(updated.getPhone());
            therapist.setSpecialty(updated.getSpecialty());
            return therapistRepository.save(therapist);
        }).orElse(null);
    }

    public void deleteById(Long id) {
        therapistRepository.deleteById(id);
    }
}
