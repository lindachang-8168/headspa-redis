package com.headspa.redis.controller;

import com.headspa.redis.model.Appointment;
import com.headspa.redis.service.AppointmentService;
import com.headspa.redis.service.TimeSlotConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id) {
        return appointmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Appointment> getByUser(@PathVariable Long userId) {
        return appointmentService.findByUserId(userId);
    }

    @GetMapping("/therapist/{therapistId}")
    public List<Appointment> getByTherapist(@PathVariable Long therapistId) {
        return appointmentService.findByTherapistId(therapistId);
    }

    @PostMapping
    public Appointment create(@RequestBody Appointment appointment) {
        return appointmentService.save(appointment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> update(@PathVariable Long id, @RequestBody Appointment appointment) {
        Appointment updated = appointmentService.update(id, appointment);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 時段衝突 → 409 Conflict，回傳 {"message": "..."} 讓前端直接顯示
    @ExceptionHandler(TimeSlotConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(TimeSlotConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }
}
