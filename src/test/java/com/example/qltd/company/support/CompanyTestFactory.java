package com.example.qltd.company.support;

import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanySize;
import com.example.qltd.company.enums.CompanyStatus;

public final class CompanyTestFactory {

    private CompanyTestFactory() {
    }

    /**
     * CreateCompanyRequest hợp lệ
     */
    public static CreateCompanyRequest createRequest() {

        CreateCompanyRequest request = new CreateCompanyRequest();

        request.setName("OpenAI");

        request.setDescription("AI Research Company");

        request.setWebsite("https://openai.com");

        request.setEmail("contact@openai.com");

        request.setPhone("0912345678");

        request.setLogoUrl("https://openai.com/logo.png");

        request.setAddress("District 1");

        request.setCity("Ho Chi Minh");

        request.setCountry("Vietnam");

        request.setFoundedYear(2015);

        request.setEmployeeCount(1000);

        request.setCompanySize(CompanySize.LARGE);

        return request;
    }

    /**
     * UpdateCompanyRequest hợp lệ
     */
    public static UpdateCompanyRequest updateRequest() {

        UpdateCompanyRequest request = new UpdateCompanyRequest();

        request.setName("OpenAI Vietnam");

        request.setDescription("Updated description");

        request.setWebsite("https://openai.vn");

        request.setEmail("hello@openai.vn");

        request.setPhone("0988888888");

        request.setLogoUrl("https://openai.vn/logo.png");

        request.setAddress("District 7");

        request.setCity("Ho Chi Minh");

        request.setCountry("Vietnam");

        request.setFoundedYear(2016);

        request.setEmployeeCount(1500);

        request.setCompanySize(CompanySize.ENTERPRISE);

        return request;
    }

    /**
     * Entity mặc định
     */
    public static Company company() {

        return Company.builder()
                .name("OpenAI")
                .slug("openai")
                .description("AI Research Company")
                .website("https://openai.com")
                .email("contact@openai.com")
                .phone("0912345678")
                .logoUrl("https://openai.com/logo.png")
                .address("District 1")
                .city("Ho Chi Minh")
                .country("Vietnam")
                .foundedYear(2015)
                .employeeCount(1000)
                .companySize(CompanySize.LARGE)
                .status(CompanyStatus.ACTIVE)
                .deleted(false)
                .build();
    }

    /**
     * Entity đã bị soft delete
     */
    public static Company deletedCompany() {

        Company company = company();

        company.setDeleted(true);

        company.setStatus(CompanyStatus.INACTIVE);

        return company;
    }

    /**
     * Response mặc định
     */
    public static CompanyResponse response() {

        return CompanyResponse.builder()
                .id(1L)
                .name("OpenAI")
                .slug("openai")
                .description("AI Research Company")
                .website("https://openai.com")
                .email("contact@openai.com")
                .phone("0912345678")
                .logoUrl("https://openai.com/logo.png")
                .address("District 1")
                .city("Ho Chi Minh")
                .country("Vietnam")
                .foundedYear(2015)
                .employeeCount(1000)
                .companySize(CompanySize.LARGE)
                .status(CompanyStatus.ACTIVE)
                .build();
    }

    /**
     * Request lỗi: name rỗng
     */
    public static CreateCompanyRequest invalidNameRequest() {

        CreateCompanyRequest request = createRequest();

        request.setName("");

        return request;
    }

    /**
     * Request lỗi: email sai
     */
    public static CreateCompanyRequest invalidEmailRequest() {

        CreateCompanyRequest request = createRequest();

        request.setEmail("abc");

        return request;
    }

    /**
     * Request lỗi: phone sai
     */
    public static CreateCompanyRequest invalidPhoneRequest() {

        CreateCompanyRequest request = createRequest();

        request.setPhone("123");

        return request;
    }

    /**
     * Request lỗi: website sai
     */
    public static CreateCompanyRequest invalidWebsiteRequest() {

        CreateCompanyRequest request = createRequest();

        request.setWebsite("abc");

        return request;
    }

}