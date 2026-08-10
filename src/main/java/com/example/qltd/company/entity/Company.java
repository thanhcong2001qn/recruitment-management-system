package com.example.qltd.company.entity;

import com.example.qltd.common.base.BaseEntity;
import com.example.qltd.company.enums.CompanySize;
import com.example.qltd.company.enums.CompanyStatus;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_company_slug", columnNames = "slug")
})
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String website;

    private String email;

    private String phone;

    @Column(name = "logo_url")
    private String logoUrl;

    private String address;

    private String city;

    private String country;

    private Integer foundedYear;

    private Integer employeeCount;

    @Enumerated(EnumType.STRING)
    private CompanySize companySize;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
