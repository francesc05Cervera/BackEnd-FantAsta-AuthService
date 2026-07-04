package com.fantasta.auth_service.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fantasta.auth_service.dto.*;
import com.fantasta.auth_service.exception.ExceptionRegistration;
import com.fantasta.auth_service.service.AuthService;
import com.fantasta.auth_service.exception.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UtenteController 
{
	private AuthService service;
	
	public UtenteController(AuthService service)
	{
		this.service = service; 
	}
	
	@PostMapping("/register")
	public ResponseEntity<Object> register(@RequestBody RegisterRequest req)
	{
		RegisterResponse res; 
	
		try
		{
			res = service.register(req);
			return ResponseEntity.ok(res);
		}
		catch(ExceptionRegistration e)
		{
			return ResponseEntity.status(409).body(e.getMessage());
		}
		
	}
	
	@PostMapping("/login")
	public ResponseEntity<Object> login(@RequestBody LoginRequest req)
	{
		LoginResponse res; 
		
		try
		{
			res = service.login(req);
			return ResponseEntity.ok(res);
		}
		catch(BadLoginException e)
		{
			return ResponseEntity.status(401).body(e.getMessage());
		}
	}
	
	@GetMapping("/me")
	public ResponseEntity<Object> me(@RequestHeader("Authorization") String authHeader) {
	    try {
	        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	            return ResponseEntity.status(400).body("Invalid Authorization header");
	        }

	        String token = authHeader.substring(7);
	        UserDTO dto = service.me(token); 
	        return ResponseEntity.ok(dto);
	    } catch (NotFoundException e) {
	        return ResponseEntity.status(404).body(e.getMessage());
	    }
	}
}
