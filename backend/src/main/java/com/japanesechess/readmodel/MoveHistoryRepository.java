package com.japanesechess.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MoveHistoryRepository extends JpaRepository<MoveHistoryEntity, Long> {
    List<MoveHistoryEntity> findByGameIdOrderByMoveNumberAsc(UUID gameId);

    List<MoveHistoryEntity> findByGameIdAndMoveNumberLessThanEqualOrderByMoveNumberAsc(
        UUID gameId, Integer moveNumber);
}
