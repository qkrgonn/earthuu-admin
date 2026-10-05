package com.earthuu.admin.masterdata;

import com.earthuu.admin.auth.entity.AdminUser;
import com.earthuu.admin.auth.entity.PasswordCredential;
import com.earthuu.admin.auth.repository.AdminUserRepository;
import com.earthuu.admin.auth.repository.PasswordCredentialRepository;
import com.earthuu.admin.event.entity.University;
import com.earthuu.admin.global.audit.AuditLogRepository;
import com.earthuu.admin.masterdata.entity.Category;
import com.earthuu.admin.masterdata.repository.CategoryRepository;
import com.earthuu.admin.masterdata.repository.MasterUniversityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminMasterDataIntegrationTests {
    private static final String ADMIN_EMAIL = "master-admin@earthuu.test";
    @Autowired MockMvc mockMvc;
    @Autowired AdminUserRepository userRepository;
    @Autowired PasswordCredentialRepository credentialRepository;
    @Autowired MasterUniversityRepository universityRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired PasswordEncoder passwordEncoder;

    AdminUser admin;
    University university;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(AdminUser.createAdmin());
        credentialRepository.save(PasswordCredential.create(admin.getId(), ADMIN_EMAIL, passwordEncoder.encode("password")));
        university = universityRepository.saveAndFlush(University.create("기존대학교", "Existing University"));
    }

    @Test
    void universitiesCanBeCreatedUpdatedFilteredAndDeactivated() throws Exception {
        mockMvc.perform(post("/api/admin/v1/master-data/universities")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameKo\":\"새대학교\",\"nameEn\":\"New University\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(patch("/api/admin/v1/master-data/universities/{id}", university.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameKo\":\"수정대학교\",\"nameEn\":\"Updated University\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.nameKo").value("수정대학교"));

        mockMvc.perform(post("/api/admin/v1/master-data/universities/{id}/deactivate", university.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/admin/v1/master-data/universities").with(user(ADMIN_EMAIL).roles("ADMIN"))
                        .queryParam("active", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].nameKo").value("수정대학교"));
    }

    @Test
    void duplicateUniversityAndDomainAreRejected() throws Exception {
        mockMvc.perform(post("/api/admin/v1/master-data/universities")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameKo\":\"기존대학교\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MASTER_DATA_ALREADY_EXISTS"));

        var body = "{\"domain\":\"school.ac.kr\"}";
        mockMvc.perform(post("/api/admin/v1/master-data/universities/{id}/domains", university.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.domain").value("school.ac.kr"));
        mockMvc.perform(post("/api/admin/v1/master-data/universities/{id}/domains", university.getId())
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void domainDeletionMeansDeactivationAndCanBeReactivated() throws Exception {
        var body = "{\"domain\":\"earthuu.ac.kr\"}";
        mockMvc.perform(post("/api/admin/v1/master-data/universities/{id}/domains", university.getId())
                .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
        mockMvc.perform(delete("/api/admin/v1/master-data/universities/{id}/domains/{domain}",
                        university.getId(), "earthuu.ac.kr")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.active").value(false));
        mockMvc.perform(post("/api/admin/v1/master-data/universities/{id}/domains/{domain}/activate",
                        university.getId(), "earthuu.ac.kr")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void citiesAndCategoriesSupportLifecycleManagement() throws Exception {
        mockMvc.perform(post("/api/admin/v1/master-data/cities")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"seoul\",\"officialName\":\"서울특별시\",\"displayNameKo\":\"서울\",\"displayNameEn\":\"Seoul\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.code").value("SEOUL"));
        mockMvc.perform(post("/api/admin/v1/master-data/cities/SEOUL/deactivate")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(post("/api/admin/v1/master-data/categories")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"culture\",\"nameKo\":\"문화\",\"nameEn\":\"Culture\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.code").value("CULTURE"));
        assertThat(categoryRepository.findByCodeIgnoreCase("culture")).map(Category::isActive).contains(true);
    }

    @Test
    void writesProduceAuditLogsAndAnonymousAccessIsBlocked() throws Exception {
        mockMvc.perform(post("/api/admin/v1/master-data/cities")
                .with(user(ADMIN_EMAIL).roles("ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"BUSAN\",\"officialName\":\"부산광역시\",\"displayNameKo\":\"부산\"}"));
        assertThat(auditLogRepository.findAll()).extracting("action").contains("CITY_CREATED");
        mockMvc.perform(get("/api/admin/v1/master-data/cities")).andExpect(status().isUnauthorized());
    }

    @Test
    void masterDataPathsAreDocumented() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/v1/master-data/universities']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/master-data/cities']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/v1/master-data/categories']").exists());
    }
}
