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

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UtenteRepository utenteRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,UtenteRepository utenteRepository) 
    {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.utenteRepository = utenteRepository;
    }
    
    public RegisterResponse register(RegisterRequest req) throws ExceptionRegistration
    {
    	Utente u = utenteRepository.findByEmail(req.getEmail());
    	
    	if(u != null)
    	{
    		throw new ExceptionRegistration("Utente già esistente con la mail"); 
    	}
    	
    	String hashpw = passwordEncoder.encode(req.getPassword());
    	
    	Utente usr = new Utente(req.getEmail(), req.getName(), req.getSurname(), hashpw);
    	utenteRepository.save(usr); 
    	
    	return new RegisterResponse(usr.getId(), usr.getEmail(), usr.getName(), usr.getSurname()); 
    }
    
    public LoginResponse login(LoginRequest req) throws BadLoginException
    {
    	Utente utente = utenteRepository.findByEmail(req.getEmail()); 
    	if(utente == null)
    	{
    		throw new BadLoginException("Utente inesistente"); 
    	}
    	
    	if(!passwordEncoder.matches(req.getPassword(), utente.getPasswordHash()))
    	{
    		throw new BadLoginException("Password Errata!");
    	}
    	
    	String token = jwtTokenProvider.generateToken(utente);
    	
    	return new LoginResponse(token, utente.getId(), utente.getEmail(), utente.getName(), utente.getSurname());
    }
    
    public UserDTO me(String token) throws NotFoundException
    {
    	Long usrID = jwtTokenProvider.getUserIdFromToken(token);
    	Utente utente = utenteRepository.findByid(usrID);
    	
    	if(utente == null) { throw new NotFoundException("Nessun utente trovato col token!"); }
    	
    	return new UserDTO(utente.getId(), utente.getEmail(), utente.getName(), utente.getSurname());
    }
}