package com.mobigen.accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // primary key 생성을 데이터베이스에 위임
    @Column(name="customer_id") // column 명 지정 (Accounts 클래스와 맵핑 계획. 현재 소스에서는 jpa 의 join 관련 annotation은 사용하진 않음)
    private Long customerId;

    private String name;

    private String email;

    @Column(name="mobile_number")
    private String mobileNumber;
}
