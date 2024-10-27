package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.VerveService;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/verve")
public class Controller {
    @Autowired
    private VerveService service;

    @GetMapping("/accept")
    public ResponseEntity<String> accept(
        @RequestParam int id,
        @RequestParam(required = false) String endpoint) {

        boolean ok = service.process(id, endpoint);
        return ok ? new ResponseEntity<>("ok", HttpStatus.OK)
                       : new ResponseEntity<>("failed", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

