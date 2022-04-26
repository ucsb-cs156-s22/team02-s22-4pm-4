package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "menuitemreview")

public class MenuItemReview {
    @Id
    private Long itemId;
    private String reviewerEmail;
    private int stars;
    private LocalDateTime localDateTime;
    private String comments;
}
