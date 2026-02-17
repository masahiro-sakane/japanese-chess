package com.japanesechess.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameViewRepository extends JpaRepository<GameViewEntity, UUID> {
}
