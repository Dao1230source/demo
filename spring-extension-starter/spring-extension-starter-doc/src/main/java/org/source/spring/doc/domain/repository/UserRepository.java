package org.source.spring.doc.domain.repository;

import org.source.jpa.repository.UnifiedJpaRepository;
import org.source.spring.doc.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends UnifiedJpaRepository<UserEntity, Long> {
}