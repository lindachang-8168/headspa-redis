package com.headspa.redis.repository;

import com.headspa.redis.model.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {
}
