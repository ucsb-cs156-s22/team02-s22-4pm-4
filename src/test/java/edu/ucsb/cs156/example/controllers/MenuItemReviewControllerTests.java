package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

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

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

        @MockBean
        MenuItemReviewRepository menuItemReviewRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/MenuItemReview/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/MenuItemReview/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/MenuItemReview/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/MenuItemReview/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                MenuItemReview reviews = MenuItemReview.builder()
                                .id(1L)
                                .itemId(27L)
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(3)
                                .dateReviewed(ldt)
                                .comments("bland af")
                                .build();

                when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.of(reviews));

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(reviews);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(menuItemReviewRepository.findById(eq(2L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview?id=2"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findById(eq(2L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("MenuItemReview with id 2 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_MenuItemReview() throws Exception {

                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");
                MenuItemReview review1 = MenuItemReview.builder()
                                .id(1L)
                                .itemId(27L)
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(3)
                                .dateReviewed(ldt)
                                .comments("bland af")
                                .build();

                MenuItemReview review2 = MenuItemReview.builder()
                                .id(2L)
                                .itemId(29L)
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(5)
                                .dateReviewed(ldt)
                                .comments("best apple pie ever")
                                .build();


                ArrayList<MenuItemReview> expectedCommons = new ArrayList<>();
                expectedCommons.addAll(Arrays.asList(review1, review2));

                when(menuItemReviewRepository.findAll()).thenReturn(expectedCommons);

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCommons);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_review() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                MenuItemReview menuItemReview1 = MenuItemReview.builder()
                    .itemId(27L)
                    .reviewerEmail("cgaucho@ucsb.edu")
                    .stars(3)
                    .dateReviewed(ldt)
                    .comments("bland af")
                    .build();
    
                when(menuItemReviewRepository.save(eq(menuItemReview1))).thenReturn(menuItemReview1);
    
                // act
                MvcResult response = mockMvc.perform(
                    post("/api/MenuItemReview/post?itemId=27&reviewerEmail=cgaucho@ucsb.edu&stars=3&dateReviewed=2022-01-03T00:00:00&comments=bland af") // NOTE
                        .with(csrf()))
                    .andExpect(status().isOk()).andReturn();
    
                // assert
                verify(menuItemReviewRepository, times(1)).save(menuItemReview1);
                String expectedJson = mapper.writeValueAsString(menuItemReview1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_review() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");
                MenuItemReview review = MenuItemReview.builder()
                                .id(1L)
                                .itemId(27L)
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(3)
                                .dateReviewed(ldt)
                                .comments("bland af")
                                .build();

                when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.of(review));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/MenuItemReview?id=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(menuItemReviewRepository, times(1)).findById(1L);
                verify(menuItemReviewRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("MenuItemReview with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_reviews_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(menuItemReviewRepository.findById(eq(2L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/MenuItemReview?id=2")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(menuItemReviewRepository, times(1)).findById(2L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("MenuItemReview with id 2 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_reviews() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");
                MenuItemReview review = MenuItemReview.builder()
                                .id(1L)
                                .itemId(27L)
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(3)
                                .dateReviewed(ldt)
                                .comments("bland af")
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-20T00:00:00");
                MenuItemReview reviewEdited = MenuItemReview.builder()
                                .id(1L)
                                .itemId(27L)
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(0)
                                .dateReviewed(ldt2)
                                .comments("not tryna get food poisoning, but if I were this would do it")
                                .build();

                String requestBody = mapper.writeValueAsString(reviewEdited);

                when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.of(review));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/MenuItemReview?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(menuItemReviewRepository, times(1)).findById(1L);
                verify(menuItemReviewRepository, times(1)).save(reviewEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_reviews_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-04-20T00:00:00");
                MenuItemReview reviewEdited = MenuItemReview.builder()
                                .id(1L)
                                .itemId(27L)
                                .reviewerEmail("cgaucho@ucsb.edu")
                                .stars(0)
                                .dateReviewed(ldt)
                                .comments("not tryna get food poisoning, but if I were this would do it")
                                .build();

                String requestBody = mapper.writeValueAsString(reviewEdited);

                when(menuItemReviewRepository.findById(eq(2L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/MenuItemReview?id=2")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(menuItemReviewRepository, times(1)).findById(2L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("MenuItemReview with id 2 not found", json.get("message"));

        }
}

