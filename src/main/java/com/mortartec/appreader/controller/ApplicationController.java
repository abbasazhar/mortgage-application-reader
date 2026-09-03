package com.mortartec.appreader.controller;

import com.mortartec.appreader.dto.ApplicationRequest;
import com.mortartec.appreader.model.MortgageApplication;
import com.mortartec.appreader.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST surface of the application reader.
 *
 *   POST /api/applications       submit a new mortgage case
 *   GET  /api/applications       list every case in local storage
 *   GET  /api/applications/{id}  read back a single case
 *
 * Designed to be exercised entirely from a terminal with curl (see README).
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<MortgageApplication> submitApplication(@Valid @RequestBody ApplicationRequest request) {
        MortgageApplication saved = applicationService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<MortgageApplication>> listApplications() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MortgageApplication> getApplication(@PathVariable String id) {
        return applicationService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
