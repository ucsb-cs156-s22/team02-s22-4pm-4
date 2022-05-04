package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;

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

@WebMvcTest(controllers = RecommendationController.class)
@Import(TestConfig.class)
public class RecommendationReviewControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRepository recommendationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/Recommendation/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/Recommendation/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/Recommendation/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/Recommendation?id=123"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/Recommendation/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Recommendation/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Recommendation/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                LocalDateTime time = LocalDateTime.parse("2022-01-03T00:00:00");

                Recommendation rec = Recommendation.builder()
                                .id(21L)
                                .requesterEmail("cgaucho@ucsb.edu")
                                .professorEmail("phtcon@ucsb.edu")
                                .explanation("salt")
                                .dateRequested(time)
                                .dateNeeded(LocalDateTime.now())
                                .done(true)
                                .build();

                when(recommendationRepository.findById(eq(21L))).thenReturn(Optional.of(rec));

                // act
                MvcResult response = mockMvc.perform(get("/api/Recommendation?id=21"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRepository, times(1)).findById(eq(21L));
                String expectedJson = mapper.writeValueAsString(rec);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(recommendationRepository.findById(eq(22L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/Recommendation?id=22"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recommendationRepository, times(1)).findById(eq(22L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Recommendation with id 22 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_Recommendation() throws Exception {

                // arrange
                LocalDateTime time = LocalDateTime.parse("2022-01-03T00:00:00");
                Recommendation rec = Recommendation.builder()
                .id(21L)
                .requesterEmail("cgaucho@ucsb.edu")
                .professorEmail("phtcon@ucsb.edu")
                .explanation("salt")
                .dateRequested(time)
                .dateNeeded(LocalDateTime.now())
                .done(true)
                .build();

                Recommendation rec2 = Recommendation.builder()
                .id(21L)
                .requesterEmail("cgaucho@ucsb.edu")
                .professorEmail("phtcon@ucsb.edu")
                .explanation("salt")
                .dateRequested(time)
                .dateNeeded(LocalDateTime.now())
                .done(true)
                .build();

                ArrayList<Recommendation> expectedCommons = new ArrayList<>();
                expectedCommons.addAll(Arrays.asList(rec, rec2));

                when(recommendationRepository.findAll()).thenReturn(expectedCommons);

                // act
                MvcResult response = mockMvc.perform(get("/api/Recommendation/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCommons);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {
                // arrange
                LocalDateTime time = LocalDateTime.parse("2022-01-03T00:00:00");
                Recommendation rec = Recommendation.builder()
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("salt")
                            .dateRequested(time)
                            .dateNeeded(time)
                            .done(true)
                            .build();

                when(recommendationRepository.save(eq(rec))).thenReturn(rec);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/Recommendation/post?requesterEmail=cgaucho@ucsb.edu&professorEmail=phtcon@ucsb.edu&explanation=salt&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-01-03T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).save(rec);
                String expectedJson = mapper.writeValueAsString(rec);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange
                LocalDateTime time = LocalDateTime.parse("2022-01-03T00:00:00");
                Recommendation rec = Recommendation.builder()
                            .id(21L)
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("salt")
                            .dateRequested(time)
                            .dateNeeded(LocalDateTime.now())
                            .done(true)
                            .build();

                when(recommendationRepository.findById(eq(21L))).thenReturn(Optional.of(rec));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Recommendation?id=21")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(21L);
                verify(recommendationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 21 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recommendationRepository.findById(eq(22L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Recommendation?id=22")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(22L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 22 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange

                LocalDateTime time = LocalDateTime.parse("2022-01-03T00:00:00");
                Recommendation recOrig = Recommendation.builder()
                            .id(21L)
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("salt")
                            .dateRequested(time)
                            .dateNeeded(LocalDateTime.now())
                            .done(true)
                            .build();

                LocalDateTime time2 = LocalDateTime.parse("2022-04-20T00:00:00");
                Recommendation recEdited = Recommendation.builder()
                            .id(21L)
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("salt")
                            .dateRequested(time2)
                            .dateNeeded(LocalDateTime.now())
                            .done(true)
                            .build();

                String requestBody = mapper.writeValueAsString(recEdited);

                when(recommendationRepository.findById(eq(21L))).thenReturn(Optional.of(recOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Recommendation?id=21")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(21L);
                verify(recommendationRepository, times(1)).save(recEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange
                LocalDateTime time = LocalDateTime.parse("2022-04-20T00:00:00");
                Recommendation editedCommons = Recommendation.builder()
                            .id(21L)
                            .requesterEmail("cgaucho@ucsb.edu")
                            .professorEmail("phtcon@ucsb.edu")
                            .explanation("salt")
                            .dateRequested(time)
                            .dateNeeded(LocalDateTime.now())
                            .done(true)
                            .build();

                String requestBody = mapper.writeValueAsString(editedCommons);

                when(recommendationRepository.findById(eq(21L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Recommendation?id=21")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(21L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 21 not found", json.get("message"));

        }
}
