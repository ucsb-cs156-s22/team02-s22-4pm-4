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
                    .tableOrBreakoutRoom("table-4")
                    .requestTime(now)
                    .explanation("help-with-team02")
                    .solved(true)
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

    @WithMockUser(roles = { "USER"})
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception{

        //arrange

        when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

        //act
        MvcResult response = mockMvc.perform(get("/api/helprequest?id=7"))
                    .andExpect(status().isNotFound()).andReturn(); 
        
        //assert

        verify(helpRequestRepository,times(1)).findById(eq(7L)); 
        Map<String, Object> json = responseToJson(response); 
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("HelpRequest with id 7 not found",json.get("message")); 
    }

    @WithMockUser(roles = { "USER" })
    @Test 
    public void logged_in_user_can_get_all_helprequests() throws Exception{
        
        //arrange 

        LocalDateTime now = LocalDateTime.now();
        HelpRequest request = HelpRequest.builder()
            .requesterEmail("victoriareed@ucsb.edu")
            .teamId("s22-4pm-4")
            .tableOrBreakoutRoom("table-4")
            .requestTime(now)
            .explanation("help-with-team02")
            .solved(false)
            .build();
        
        HelpRequest request1 = HelpRequest.builder()
            .requesterEmail("v@ucsb.edu")
            .teamId("s22-4pm-4")
            .tableOrBreakoutRoom("table-4")
            .requestTime(now)
            .explanation("help-with-team02")
            .solved(false)
            .build();
        
        ArrayList<HelpRequest> expectedRequests = new ArrayList<>(); 
        expectedRequests.addAll(Arrays.asList(request,request1)); 

        when(helpRequestRepository.findAll()).thenReturn(expectedRequests); 

        //act
        MvcResult response = mockMvc.perform(get("/api/helprequest/all"))
                    .andExpect(status().isOk()).andReturn(); 
        
        //assert 

        verify(helpRequestRepository,times(1)).findAll(); 
        String expectedJson = mapper.writeValueAsString(expectedRequests); 
        String responseString = response.getResponse().getContentAsString(); 
        assertEquals(expectedJson,responseString); 
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_request() throws Exception{
        //arrange
        LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");
        HelpRequest request = HelpRequest.builder()
                    .requesterEmail("victoriareed@ucsb.edu")
                    .teamId("s22-4pm-4")
                    .tableOrBreakoutRoom("table-4")
                    .requestTime(ldt2)
                    .explanation("help-with-team02")
                    .solved(true)
                    .build();
        
        when(helpRequestRepository.save(eq(request))).thenReturn(request); 

        //act
        MvcResult response = mockMvc.perform(
                        post("/api/helprequest/post?requesterEmail=victoriareed@ucsb.edu&teamId=s22-4pm-4&tableOrBreakoutRoom=table-4&requestTime=2022-03-11T00:00:00&explanation=help-with-team02&solved=true")
                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn(); 
        
        //assert
        verify(helpRequestRepository,times(1)).save(request); 
        String expectedJson = mapper.writeValueAsString(request);
        String responseString = response.getResponse().getContentAsString(); 
        assertEquals(expectedJson, responseString);

    }
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_request() throws Exception{
        //arrange

        LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");
        HelpRequest request = HelpRequest.builder()
                    .requesterEmail("victoriareed@ucsb.edu")
                    .teamId("s22-4pm-4")
                    .tableOrBreakoutRoom("table-4")
                    .requestTime(ldt2)
                    .explanation("help-with-team02")
                    .solved(false)
                    .build();
        
        when(helpRequestRepository.findById(eq(1L))).thenReturn(Optional.of(request));

        //act
        MvcResult response = mockMvc.perform(
                        delete("/api/helprequest?id=1")
                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn(); 
        
         //assert
         verify(helpRequestRepository,times(1)).findById(1L); 
         verify(helpRequestRepository,times(1)).delete(any()); 

         Map<String,Object> json = responseToJson(response); 
         assertEquals("HelpRequest with id 1 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_request_and_gets_right_error_message() throws Exception{
        //arrange

        when(helpRequestRepository.findById(eq(4L))).thenReturn(Optional.empty()); 

        //act
        MvcResult response = mockMvc.perform(
                        delete("/api/helprequest?id=4")
                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn(); 
        
        //assert
        verify(helpRequestRepository,times(1)).findById(4L); 
        Map<String,Object> json = responseToJson(response); 
        assertEquals("HelpRequest with id 4 not found", json.get("message"));
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_request() throws Exception {
                // arrange

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt3 = LocalDateTime.parse("2022-04-11T00:00:00");
                HelpRequest requestOrig = HelpRequest.builder()
                    .requesterEmail("victoriareed@ucsb.edu")
                    .teamId("s22-4pm-4")
                    .tableOrBreakoutRoom("table-4")
                    .requestTime(ldt2)
                    .explanation("help-with-team02")
                    .solved(false)
                    .build();

                HelpRequest requestEdited = HelpRequest.builder()
                                .requesterEmail("victoria@ucsb.edu")
                                .teamId("s22-4pm")
                                .tableOrBreakoutRoom("table-44")
                                .requestTime(ldt3)
                                .explanation("help-team02")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(requestEdited);

                when(helpRequestRepository.findById(eq(1L))).thenReturn(Optional.of(requestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(1L);
                verify(helpRequestRepository, times(1)).save(requestEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_request_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");
                HelpRequest requestEdited = HelpRequest.builder()
                    .requesterEmail("ctoriareed@ucsb.edu")
                    .teamId("s22-4pm-4")
                    .tableOrBreakoutRoom("table-43")
                    .requestTime(ldt2)
                    .explanation("help-with-team02")
                    .solved(false)
                    .build();

                String requestBody = mapper.writeValueAsString(requestEdited);

                when(helpRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(1L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 1 not found", json.get("message"));

        }
}

