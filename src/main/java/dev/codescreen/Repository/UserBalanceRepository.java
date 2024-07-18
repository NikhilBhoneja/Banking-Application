package dev.codescreen.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.codescreen.model.entity.UserBalance;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, String> {
}