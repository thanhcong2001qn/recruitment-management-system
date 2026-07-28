package com.example.qltd.company.dto.request;

import com.example.qltd.company.enums.CompanySize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Pattern(
            regexp = "^(https?://).*$",
            message = "Website must start with http:// or https://"
    )
    private String website;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9,10}$",
            message = "Invalid Vietnamese phone number"
    )
    private String phone;

    private String logoUrl;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    private CompanySize companySize;

    @Min(value = 1800, message = "Founded year is invalid")
    @Max(value = 2100, message = "Founded year is invalid")
    private Integer foundedYear;

    @PositiveOrZero(message = "Employee count must be positive or zero")
    private Integer employeeCount;
}
