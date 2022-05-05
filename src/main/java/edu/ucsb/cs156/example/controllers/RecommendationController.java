package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;
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

import java.time.LocalDateTime;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;


@Api(description = "Recommendation")
@RequestMapping("/api/Recommendation")
@RestController
@Slf4j
public class RecommendationController extends ApiController {

    @Autowired
    RecommendationRepository recommendationRepository;

    @ApiOperation(value = "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Recommendation> allRecommendations() {
        Iterable<Recommendation> recs = recommendationRepository.findAll();
        return recs;
    }

    @ApiOperation(value = "Get a single recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Recommendation getById(
            @ApiParam("id") @RequestParam Long id) {
        Recommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));

        return rec;
    }

    @ApiOperation(value = "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Recommendation postRecommendation(
        @ApiParam("requesterEmail") @RequestParam String requesterEmail,
        @ApiParam("professorEmail") @RequestParam String professorEmail,
        @ApiParam("explanation") @RequestParam String explanation,
        @ApiParam("dateRequested") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
        @ApiParam("dateNeeded") @RequestParam LocalDateTime dateNeeded,
        @ApiParam("done") @RequestParam boolean done
        ) throws JsonProcessingException{

            log.info("localDateTime={}", dateRequested);
        Recommendation temprec = new Recommendation();
        temprec.setRequesterEmail(requesterEmail);
        temprec.setProfessorEmail(professorEmail);
        temprec.setExplanation(explanation);
        temprec.setDateRequested(dateRequested);
        temprec.setDateNeeded(dateNeeded);
        temprec.setDone(done);

        Recommendation rec = recommendationRepository.save(temprec);

        return rec;
    }

    @ApiOperation(value = "Delete a Recommendation Request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRecommendation(
        @ApiParam("id") @RequestParam Long id) {
        Recommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));

        recommendationRepository.delete(rec);
        return genericMessage("Recommendation with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single recommendation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Recommendation updateRecommendations(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Recommendation incoming) {

        Recommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));


        rec.setRequesterEmail(incoming.getRequesterEmail());  
        rec.setProfessorEmail(incoming.getProfessorEmail());
        rec.setExplanation(incoming.getExplanation());
        rec.setDateRequested(incoming.getDateRequested());
        rec.setDateNeeded(incoming.getDateNeeded());
        rec.setDone(incoming.getDone());

        recommendationRepository.save(rec);

        return rec;
    }
}