package com.myproject.obs.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveRepository extends JpaRepository<Live, Long> {
    Optional<Live> findFirstByOrderByIdDesc();

    List<Live> findByLive(boolean isLive);
}
