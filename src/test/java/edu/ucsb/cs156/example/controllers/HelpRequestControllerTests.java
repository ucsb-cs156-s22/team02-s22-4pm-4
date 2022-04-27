package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import lombok.With;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase{

    @MockBean 
    HelpRequestRepository helpRequestRepository; 

    @MockBean
    UserRepository userRepository; 

    //authorization tests for /api/helprequest/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception{
        mockMvc.perform(get("/api/helprequest/all"))
                .andExpect(status().is(403)); 
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception{
        mockMvc.perform(get("/api/helprequest/all"))
                .andExpect(status().is(200)); 
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception{
        mockMvc.perform(get("/api/helprequest?id=7"))
                .andExpect(status().is(403)); 
    }

    //authorization tests for /api/helprequest/post
    
    @Test
    public void logged_out_users_cannot_post() throws Exception{
        mockMvc.perform(post("/api/helprequest/post"))
                .andExpect(status().is(403)); 
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception{
        mockMvc.perform(post("/api/helprequest/post"))
                .andExpect(status().is(403)); 
    }

    //tests with mocks for database actions 

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception{

        //arrange
        LocalDateTime now = LocalDateTime.now();
        HelpRequest request = HelpRequest.builder()
                    .requesterEmail("victoriareed@ucsb.edu")
                    .teamId("s22-4pm-4")
                    .tableOrBreakoutRoom("table 4")
                    .requestTime(now)
                    .explanation("help with team02")
                    .solved(false)
                    .build();
        when(helpRequestRepository.findById(eq(1L))).thenReturn(Optional.of(request));

        //act
        MvcResult response = mockMvc.perform(get("/api/helprequest?id=1"))
                    .andExpect(status().isOk()).andReturn(); 
        
        //assert
        verify(helpRequestRepository,times(1)).findById(eq(1L)); 
        String expectedJson = mapper.writeValueAsString(request); 
        String responseString = response.getResponse().getContentAsString(); 
        assertEquals(expectedJson, responseString);

    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_request() throws Exception{
        //arrange
        LocalDateTime now = LocalDateTime.now();
        HelpRequest request = HelpRequest.builder()
                    .requesterEmail("victoriareed@ucsb.edu")
                    .teamId("s22-4pm-4")
                    .tableOrBreakoutRoom("table 4")
                    .requestTime(now)
                    .explanation("help with team02")
                    .solved(false)
                    .build();
        
        when(helpRequestRepository.save(eq(request))).thenReturn(request); 

        //act
        MvcResult response = mockMvc.perform(
            
        )

    }
    
}
