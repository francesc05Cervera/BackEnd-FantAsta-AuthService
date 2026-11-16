/*
 * Scritto da Cervera Francesco
 * (C) Diritti Riservati
 * 
 */


package com.fantasta.auth_service.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fantasta.auth_service.dto.*;
import com.fantasta.auth_service.exception.ExceptionRegistration;
import com.fantasta.auth_service.service.AuthService;
import com.fantasta.auth_service.exception.*;

@RestController
@RequestMapping("/api/user")
public class UtenteController 
{
	private AuthService service;
	
	public UtenteController(AuthService service)
	{
		this.service = service; 
	}
	
	/*
	 * Metodo che riceve la richiesta HTTP POST e delega al metodo service la verifica e registrazione 
	 * @param RegisterRequest
	 * @return 200 OK con JSON della response creata nel service oppure 409 CONFLICT con il messaggio di errore catchato 
	 */
	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) throws ExceptionRegistration {
		return ResponseEntity.ok(service.register(req));
	}
	
	/*
	 * Metodo che riceve la login request e delega al service l'autenticazione e generazione del JWT
	 * @param LoginRequest
	 * @return 200 OK con il JSON della reponse che conterrà anche il JWT generato oppure 401 con il messaggio di errore 
	 */
	
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) throws BadLoginException {
		return ResponseEntity.ok(service.login(req));
	}
	
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal Long userId) throws NotFoundException {
        return ResponseEntity.ok(service.me(userId));
    }
}
