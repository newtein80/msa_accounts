package com.mobigen.accounts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.mobigen.accounts.entity.Accounts;

import jakarta.transaction.Transactional;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    // Optional<T>는 null이 올 수 있는 값을 감싸는 Wrapper 클래스로 참조하더라도 NullPonterException이 발생하지 않도록 도와준다.
    // findBy필드명, deleteBy필드명, findAll, findBy필드명And필드명 등을 기본으로 제공한다.
    Optional<Accounts> findByCustomerId(Long customerId);

    // @Modifying 애노테이션은 기본적으로 @Transactional과 함께 사용
    @Transactional
    @Modifying
    void deleteByCustomerId(Long customerId);

}
