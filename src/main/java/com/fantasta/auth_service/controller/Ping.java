package com.fantasta.auth_service.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth/ping")
@CrossOrigin(origins = "*")
public class Ping 
{
	@GetMapping
	public ResponseEntity<Object> ping()
	{
		return ResponseEntity.ok("Ping OK"); 
	}
	
}
