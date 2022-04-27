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
            @ApiParam("itemId") @RequestParam Long itemId) {
                MenuItemReview reviews = menuItemReviewRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, itemId));

        return reviews;
    }

    @ApiOperation(value = "Create a new review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postReviews(
        @ApiParam("itemId") @RequestParam Long itemId,
        @ApiParam("reviewerEmail") @RequestParam String reviewerEmail,
        @ApiParam("stars") @RequestParam int stars,
        @ApiParam("dateReviewed") @RequestParam LocalDateTime dateReviewed
        )
        {

        MenuItemReview reviews = new MenuItemReview();
        reviews.setItemId(itemId);
        reviews.setReviewerEmail(reviewerEmail);
        reviews.setStars(stars);
        reviews.setDateReviewed(dateReviewed);

        MenuItemReview savedreviews = menuItemReviewRepository.save(reviews);

        return savedreviews;
    }

    @ApiOperation(value = "Delete a review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteReviews(
            @ApiParam("itemId") @RequestParam Long itemId) {
                MenuItemReview reviews = menuItemReviewRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, itemId));

        menuItemReviewRepository.delete(reviews);
        return genericMessage("MenuItemReview with id %s deleted".formatted(itemId));
    }

    @ApiOperation(value = "Update a single review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateReviews(
            @ApiParam("itemId") @RequestParam Long itemId,
            @RequestBody @Valid MenuItemReview incoming) {

                MenuItemReview reviews = menuItemReviewRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, itemId));


        reviews.setItemId(incoming.getItemId());  
        reviews.setReviewerEmail(incoming.getReviewerEmail());
        reviews.setStars(incoming.setStars());
        reviews.setDateReviewed(incoming.getDateReviewed());

        menuItemReviewRepository.save(reviews);

        return reviews;
    }
}