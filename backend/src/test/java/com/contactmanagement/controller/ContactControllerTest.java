package com.contactmanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contactmanagement.dto.ContactEmailDTO;
import com.contactmanagement.dto.ContactPhoneDTO;
import com.contactmanagement.dto.ContactResponse;
import com.contactmanagement.entity.User;
import com.contactmanagement.exception.GlobalExceptionHandler;
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.service.AuthenticationHelper;
import com.contactmanagement.service.ContactService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Standalone MockMvc keeps controller calls wired to explicit mocks (avoids slice/full-context
 * quirks where the controller sometimes still sees a real {@link ContactService}).
 */
@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactService contactService;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @BeforeEach
    void setUp() {
        ContactController controller = new ContactController(contactService, authenticationHelper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private User sampleUser() {
        User u = new User();
        u.setId(10L);
        u.setEmail("owner@example.com");
        return u;
    }

    private static RequestPostProcessor authenticatedUser() {
        return user("owner@example.com").roles("USER");
    }

    @Test
    void createContactReturns201() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        ContactResponse body = ContactResponse.builder()
                .id(1L)
                .firstName("A")
                .lastName("B")
                .title("Ms")
                .emails(List.of(new ContactEmailDTO(1L, "a@b.com", "WORK")))
                .phones(List.of(new ContactPhoneDTO(1L, "123", "HOME")))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(contactService.createContact(any(User.class), any())).thenReturn(body);

        mockMvc.perform(post("/api/contacts")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "A",
                                  "lastName": "B",
                                  "title": "Ms",
                                  "emails": [{"email": "a@b.com", "label": "work"}],
                                  "phones": [{"phoneNumber": "123", "label": "home"}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("A"));

        verify(contactService).createContact(any(User.class), any());
    }

    @Test
    void getContactReturns404FromHandler() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        when(contactService.getContactById(eq(99L), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Contact not found"));

        mockMvc.perform(get("/api/contacts/99").with(authenticatedUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found"));
    }

    @Test
    void listContactsReturnsPage() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        Page<ContactResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0);
        when(contactService.getAllContacts(any(User.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/contacts")
                        .with(authenticatedUser())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deleteContactReturnsOk() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        doNothing().when(contactService).deleteContact(eq(5L), any(User.class));

        mockMvc.perform(delete("/api/contacts/5").with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contact deleted successfully"));
    }

    @Test
    void updateContactReturns400WhenLabelInvalid() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        when(contactService.updateContact(eq(1L), any(User.class), any()))
                .thenThrow(new IllegalArgumentException("Invalid email label: use work, personal, or other"));

        mockMvc.perform(put("/api/contacts/1")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "A",
                                  "lastName": "B",
                                  "emails": [{"email": "a@b.com", "label": "invalid"}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getContactByIdReturns200() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        ContactResponse body = ContactResponse.builder()
                .id(3L)
                .firstName("Jane")
                .lastName("Doe")
                .title("Director")
                .emails(List.of())
                .phones(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(contactService.getContactById(eq(3L), any(User.class))).thenReturn(body);

        mockMvc.perform(get("/api/contacts/3").with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void searchContactsReturnsPage() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        ContactResponse hit = ContactResponse.builder()
                .id(2L)
                .firstName("Alice")
                .lastName("Smith")
                .emails(List.of())
                .phones(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Page<ContactResponse> page = new PageImpl<>(List.of(hit), PageRequest.of(0, 10), 1);
        when(contactService.searchContacts(any(User.class), eq("alice"), any())).thenReturn(page);

        mockMvc.perform(get("/api/contacts/search")
                        .with(authenticatedUser())
                        .param("searchTerm", "alice")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }

    @Test
    void updateContactReturns200() throws Exception {
        when(authenticationHelper.getAuthenticatedUser(any())).thenReturn(sampleUser());
        ContactResponse body = ContactResponse.builder()
                .id(1L)
                .firstName("Updated")
                .lastName("Name")
                .title("Lead")
                .emails(List.of(new ContactEmailDTO(1L, "a@b.com", "WORK")))
                .phones(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(contactService.updateContact(eq(1L), any(User.class), any())).thenReturn(body);

        mockMvc.perform(put("/api/contacts/1")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Updated",
                                  "lastName": "Name",
                                  "title": "Lead",
                                  "emails": [{"email": "a@b.com", "label": "work"}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }
}
