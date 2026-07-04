package com.fantasta.auth_service.repository;

import com.fantasta.auth_service.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

	Utente findByEmail(String email);
	Utente findByid(Long id);
}