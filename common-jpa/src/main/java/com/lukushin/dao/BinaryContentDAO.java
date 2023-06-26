package com.lukushin.dao;

import com.lukushin.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long> {
}
