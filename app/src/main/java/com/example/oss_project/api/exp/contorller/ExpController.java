package com.oss_project.api.exp.controller;

import com.oss_project.api.exp.dto.ExpEventRequest;
import com.oss_project.api.exp.dto.ExpEventResponse;
import com.oss_project.api.exp.service.ExpService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exp")
public class ExpController {

    private final ExpService expService;

    public ExpController(ExpService expService) {
        this.expService = expService;
    }

    @PostMapping("/events")
    public ExpEventResponse addExp(@RequestBody ExpEventRequest request) {
        return expService.addExp(request);
    }
}