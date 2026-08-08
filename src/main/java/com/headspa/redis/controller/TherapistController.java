package com.headspa.redis.controller;

import com.headspa.redis.model.Therapist;
import com.headspa.redis.service.TherapistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapists")
public class TherapistController {

    @Autowired
    private TherapistService therapistService;

    @GetMapping
    public List<Therapist> getAll() {
        return therapistService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Therapist> getById(@PathVariable Long id) {
        return therapistService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Therapist create(@RequestBody Therapist therapist) {
        return therapistService.save(therapist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Therapist> update(@PathVariable Long id, @RequestBody Therapist therapist) {
        Therapist updated = therapistService.update(id, therapist);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        therapistService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
