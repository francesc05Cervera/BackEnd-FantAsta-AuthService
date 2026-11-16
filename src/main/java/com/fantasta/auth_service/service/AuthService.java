/*
 * Scritto da Cervera Francesco
 * (C) Diritti riservati
 * 
 */



package com.fantasta.auth_service.service;

import com.fantasta.auth_service.dto.RegisterRequest;
import com.fantasta.auth_service.dto.RegisterResponse;
import com.fantasta.auth_service.dto.LoginRequest;
import com.fantasta.auth_service.dto.LoginResponse;
import com.fantasta.auth_service.dto.UserDTO;
import com.fantasta.auth_service.model.Utente;
import com.fantasta.auth_service.repository.UtenteRepository;
import com.fantasta.auth_service.security.JwtTokenProvider;
import com.fantasta.auth_service.exception.ExceptionRegistration;
import com.fantasta.auth_service.exception.NotFoundException;
import com.fantasta.auth_service.exception.BadLoginException;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UtenteRepository utenteRepository;
    private final String unknownUserPasswordHash;

    public AuthService(JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,UtenteRepository utenteRepository) 
    {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.utenteRepository = utenteRepository;
        this.unknownUserPasswordHash = passwordEncoder.encode("unused-password");
    }
    
    
    /*
     * Metodo di validazione dati e creazione di un utente a seguito di una richiesta di registrazione
     * @param Richiesta di registrazione pervenuta dal client 
     * @return La risposta da inviare al client con le informazioni sull'utente (compreso l'ID autogenerato dal DB) 
     * 
     * Può generare la eccezione se esiste già un utente con la stessa email
     */
    public RegisterResponse register(RegisterRequest req) throws ExceptionRegistration
    {
    	Utente u = utenteRepository.findByEmail(req.getEmail());
    	
    	if(u != null)
    	{
    		throw new ExceptionRegistration("Utente già esistente con la mail"); 
    	}
    	
    	String hashpw = passwordEncoder.encode(req.getPassword());
    	
    	Utente usr = new Utente(req.getEmail(), req.getName(), req.getSurname(), hashpw);
        try {
            utenteRepository.saveAndFlush(usr);
        } catch (DataIntegrityViolationException e) {
            if (utenteRepository.findByEmail(req.getEmail()) != null) {
                throw new ExceptionRegistration("Utente già esistente con la mail");
            }
            throw e;
        }
    	
    	return new RegisterResponse(usr.getId(), usr.getEmail(), usr.getName(), usr.getSurname()); 
    }
    
    /*
     * Metodo per gestire la richiesta di login pervenuta dal client 
     * 
     * @param Richiesta di login con email e password
     * @return Risposta da inviare al client con JWT 
     * 
     * Può generare eccezione se la mail non esiste o la password inserita non matcha con la password crittografata nel DB
     */
    public LoginResponse login(LoginRequest req) throws BadLoginException
    {
        Utente utente = utenteRepository.findByEmail(req.getEmail());
        String hash = utente == null ? unknownUserPasswordHash : utente.getPasswordHash();
        if (!passwordEncoder.matches(req.getPassword(), hash) || utente == null) {
            throw new BadLoginException("Credenziali non valide");
        }

        String token = jwtTokenProvider.generateToken(utente);
    	
    	return new LoginResponse(token, utente.getId(), utente.getEmail(), utente.getName(), utente.getSurname());
    }
    
    /* 
     * Metodo per inviare le informazioni dell'utente a seguito della richiesta del client al path /me 
     * @param usrID ID dell’utente estratto dal token verificato
     * @return UserDTO con tutte le informazioni dell'utente recuperato
     * 
     * Può generare una eccezione se l'utente non è stato trovato 
     */
    public UserDTO me(Long usrID) throws NotFoundException
    {
    	Utente utente = utenteRepository.findByid(usrID);
    	
    	if(utente == null) { throw new NotFoundException("Nessun utente trovato col token!"); }
    	
    	return new UserDTO(utente.getId(), utente.getEmail(), utente.getName(), utente.getSurname());
    }
}