package com.japanesechess.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/clear-database")
    @Transactional
    public ResponseEntity<Map<String, String>> clearDatabase() {
        // PostgreSQLのテーブルをクリア
        entityManager.createNativeQuery("TRUNCATE TABLE games, game_views, move_history RESTART IDENTITY CASCADE").executeUpdate();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Database cleared successfully");

        return ResponseEntity.ok(response);
    }
}
