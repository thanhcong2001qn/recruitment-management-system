package com.example.qltd.company.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.qltd.config.AbstractIntegrationTest;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanyStatus;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.company.support.CompanyTestFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class CompanyControllerIT extends AbstractIntegrationTest {
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {

        companyRepository.deleteAll();

    }

    @Nested
    @DisplayName("POST /api/companies")
    class CreateCompanyTest {

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should create company successfully")
        void shouldCreateCompanySuccessfully()
                throws Exception {

            CreateCompanyRequest request = CompanyTestFactory.createRequest();

            mockMvc.perform(

                    post("/api/companies")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isCreated())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.message")
                            .value("Company created successfully"))

                    .andExpect(jsonPath("$.data.name")
                            .value("OpenAI"))

                    .andExpect(jsonPath("$.data.email")
                            .value("contact@openai.com"))

                    .andExpect(jsonPath("$.data.city")
                            .value("Ho Chi Minh"));

            assertThat(companyRepository.count())
                    .isEqualTo(1);

            Optional<Company> company = companyRepository.findByName("OpenAI");

            assertThat(company).isPresent();

            assertThat(company.get().getSlug())
                    .isEqualTo("openai");

            assertThat(company.get().getDeleted())
                    .isFalse();

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return conflict when company name already exists")
        void shouldReturnConflictWhenDuplicateName()
                throws Exception {

            Company company = CompanyTestFactory.company();

            companyRepository.save(company);

            CreateCompanyRequest request = CompanyTestFactory.createRequest();

            mockMvc.perform(

                    post("/api/companies")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isConflict())

                    .andExpect(jsonPath("$.error")
                            .value("DUPLICATE_RESOURCE"))

                    .andExpect(jsonPath("$.message")
                            .exists());

            assertThat(companyRepository.count())
                    .isEqualTo(1);

        }

        @Test
        @DisplayName("Should return validation error when name is blank")
        void shouldReturnValidationErrorWhenNameBlank()
                throws Exception {

            CreateCompanyRequest request = CompanyTestFactory.invalidNameRequest();

            mockMvc.perform(

                    post("/api/companies")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.error")
                            .value("VALIDATION_ERROR"))

                    .andExpect(jsonPath("$.errors.name")
                            .exists());

            assertThat(companyRepository.count())
                    .isZero();

        }

        @Test
        @DisplayName("Should return validation error when email is invalid")
        void shouldReturnValidationErrorWhenEmailInvalid()
                throws Exception {

            CreateCompanyRequest request = CompanyTestFactory.invalidEmailRequest();

            mockMvc.perform(

                    post("/api/companies")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.errors.email")
                            .exists());

        }

        @Test
        @DisplayName("Should return validation error when phone is invalid")
        void shouldReturnValidationErrorWhenPhoneInvalid()
                throws Exception {

            CreateCompanyRequest request = CompanyTestFactory.invalidPhoneRequest();

            mockMvc.perform(

                    post("/api/companies")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.errors.phone")
                            .exists());

        }

        @Test
        @DisplayName("Should return validation error when website is invalid")
        void shouldReturnValidationErrorWhenWebsiteInvalid()
                throws Exception {

            CreateCompanyRequest request = CompanyTestFactory.invalidWebsiteRequest();

            mockMvc.perform(

                    post("/api/companies")

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.errors.website")
                            .exists());

        }

    }

    @Nested
    @DisplayName("GET /api/companies/{id}")
    class GetCompanyByIdTest {

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return company by id")
        void shouldReturnCompanyById() throws Exception {

            Company saved = companyRepository.save(
                    CompanyTestFactory.company());

            mockMvc.perform(

                    get("/api/companies/{id}", saved.getId())

            )

                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success").value(true))

                    .andExpect(jsonPath("$.data.id")
                            .value(saved.getId()))

                    .andExpect(jsonPath("$.data.name")
                            .value("OpenAI"))

                    .andExpect(jsonPath("$.data.slug")
                            .value("openai"));

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return 404 when company does not exist")
        void shouldReturn404WhenCompanyNotFound() throws Exception {

            mockMvc.perform(

                    get("/api/companies/{id}", 999L)

            )

                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.error")
                            .value("RESOURCE_NOT_FOUND"));

        }

    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Nested
    @DisplayName("PUT /api/companies/{id}")
    class UpdateCompanyTest {
        @Test
        @DisplayName("Should update company successfully")
        void shouldUpdateCompanySuccessfully() throws Exception {

            Company saved = companyRepository.save(
                    CompanyTestFactory.company());

            UpdateCompanyRequest request = CompanyTestFactory.updateRequest();

            mockMvc.perform(

                    put("/api/companies/{id}", saved.getId())

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.data.name")
                            .value("OpenAI Vietnam"))

                    .andExpect(jsonPath("$.data.slug")
                            .value("openai-vietnam"));

            Company updated = companyRepository.findById(saved.getId()).orElseThrow();

            assertThat(updated.getName())
                    .isEqualTo("OpenAI Vietnam");

            assertThat(updated.getSlug())
                    .isEqualTo("openai-vietnam");

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return 404 when updating non-existing company")
        void shouldReturn404WhenUpdatingNotFound() throws Exception {

            UpdateCompanyRequest request = CompanyTestFactory.updateRequest();

            mockMvc.perform(

                    put("/api/companies/{id}", 999L)

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.error")
                            .value("RESOURCE_NOT_FOUND"));

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return conflict when updating duplicate name")
        void shouldReturnConflictWhenDuplicateName() throws Exception {

            Company company1 = CompanyTestFactory.company();

            companyRepository.save(company1);

            Company company2 = Company.builder()
                    .name("Microsoft")
                    .slug("microsoft")
                    .email("microsoft@test.com")
                    .build();

            company2 = companyRepository.save(company2);

            UpdateCompanyRequest request = CompanyTestFactory.updateRequest();

            request.setName("OpenAI");

            mockMvc.perform(

                    put("/api/companies/{id}", company2.getId())

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isConflict())

                    .andExpect(jsonPath("$.error")
                            .value("DUPLICATE_RESOURCE"));

        }

        @Test
        @DisplayName("Should return validation error")
        void shouldReturnValidationError() throws Exception {

            Company saved = companyRepository.save(
                    CompanyTestFactory.company());

            UpdateCompanyRequest request = CompanyTestFactory.updateRequest();

            request.setEmail("abc");

            mockMvc.perform(

                    put("/api/companies/{id}", saved.getId())

                            .contentType(MediaType.APPLICATION_JSON)

                            .content(
                                    objectMapper.writeValueAsString(request))

            )

                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.error")
                            .value("VALIDATION_ERROR"))

                    .andExpect(jsonPath("$.errors.email")
                            .exists());

        }
    }

    @Nested
    @DisplayName("DELETE /api/companies/{id}")
    class DeleteCompanyTest {

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should soft delete company successfully")
        void shouldDeleteCompanySuccessfully() throws Exception {

            Company company = companyRepository.save(
                    CompanyTestFactory.company());

            mockMvc.perform(
                    delete("/api/companies/{id}", company.getId()))
                    .andExpect(status().isOk());

            Company deleted = companyRepository.findById(company.getId())
                    .orElseThrow();

            assertThat(deleted.getDeleted()).isTrue();

            assertThat(deleted.getStatus())
                    .isEqualTo(CompanyStatus.INACTIVE);

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return 404 when deleting non-existing company")
        void shouldReturn404WhenDeletingNotFound() throws Exception {

            mockMvc.perform(
                    delete("/api/companies/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error")
                            .value("RESOURCE_NOT_FOUND"));

        }

    }

    @Nested
    @DisplayName("PATCH /api/companies/{id}/restore")
    class RestoreCompanyTest {

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should restore company successfully")
        void shouldRestoreCompanySuccessfully() throws Exception {

            Company company = CompanyTestFactory.deletedCompany();

            company = companyRepository.save(company);

            mockMvc.perform(
                    post("/api/companies/{id}/restore", company.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            Company restored = companyRepository.findById(company.getId())
                    .orElseThrow();

            assertThat(restored.getDeleted()).isFalse();

            assertThat(restored.getStatus())
                    .isEqualTo(CompanyStatus.ACTIVE);

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return 404 when restoring non-existing company")
        void shouldReturn404WhenRestoreNotFound() throws Exception {

            mockMvc.perform(
                    post("/api/companies/{id}/restore", 999L))
                    .andExpect(status().isNotFound());

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return bad request when company is not deleted")
        void shouldReturnBadRequestWhenCompanyNotDeleted() throws Exception {

            Company company = companyRepository.save(
                    CompanyTestFactory.company());

            mockMvc.perform(
                    post("/api/companies/{id}/restore", company.getId()))
                    .andExpect(status().isBadRequest());

        }

    }

    @Nested
    @DisplayName("GET /api/companies")
    class SearchCompanyTest {

        @BeforeEach
        void initData() {

            companyRepository.deleteAll();

            companyRepository.save(
                    CompanyTestFactory.company());

            Company microsoft = CompanyTestFactory.company();

            microsoft.setName("Microsoft");

            microsoft.setSlug("microsoft");

            microsoft.setEmail("microsoft@test.com");

            companyRepository.save(microsoft);

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should return first page")
        void shouldReturnFirstPage() throws Exception {

            mockMvc.perform(
                    get("/api/companies")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success")
                            .value(true))

                    .andExpect(jsonPath("$.data.items")
                            .isArray())

                    .andExpect(jsonPath("$.data.totalElements")
                            .value(2));

        }

        @WithMockUser(username = "admin", roles = "ADMIN")
        @Test
        @DisplayName("Should search by keyword")
        void shouldSearchByKeyword() throws Exception {

            mockMvc.perform(
                    get("/api/companies")
                            .param("keyword", "Micro"))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.data.items.length()")
                            .value(1))

                    .andExpect(jsonPath("$.data.items[0].name")
                            .value("Microsoft"));

        }

    }
}
