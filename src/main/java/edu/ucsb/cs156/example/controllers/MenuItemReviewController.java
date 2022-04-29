package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;


@Api(description = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    @ApiOperation(value = "List all reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allReviews() {
        Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
        return reviews;
    }

    @ApiOperation(value = "Get a single review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @ApiParam("id") @RequestParam Long id) {
                MenuItemReview reviews = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return reviews;
    }

    @ApiOperation(value = "Create a new review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postMenuItemReview(
            @ApiParam("itemId") @RequestParam Long itemId,
            @ApiParam("reviewerEmail") @RequestParam String reviewerEmail,
            @ApiParam("stars") @RequestParam int stars,
            @ApiParam("comments") @RequestParam String comments,
            @ApiParam("dateReviewed (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateReviewed") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime localDateTime)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("localDateTime={}", localDateTime);

        MenuItemReview review = new MenuItemReview();
        review.setItemId(itemId);
        review.setReviewerEmail(reviewerEmail);
        review.setStars(stars);
        review.setComments(comments);
        review.setDateReviewed(localDateTime);

        MenuItemReview savedreview = menuItemReviewRepository.save(review);

        return savedreview;
    }

    @ApiOperation(value = "Delete a review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteReviews(
            @ApiParam("id") @RequestParam Long id) {
                MenuItemReview reviews = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        menuItemReviewRepository.delete(reviews);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateReviews(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid MenuItemReview incoming) {

                MenuItemReview reviews = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));


        reviews.setItemId(incoming.getItemId());  
        reviews.setReviewerEmail(incoming.getReviewerEmail());
        reviews.setStars(incoming.getStars());
        reviews.setDateReviewed(incoming.getDateReviewed());
        reviews.setComments(incoming.getComments());

        menuItemReviewRepository.save(reviews);

        return reviews;
    }
}