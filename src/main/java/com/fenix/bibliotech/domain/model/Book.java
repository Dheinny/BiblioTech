package com.fenix.bibliotech.domain.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message =  "{book.title.required}")
    private String title;

    @NotBlank(message = "{book.author.required}")
    private String author;

    @Column(unique = true)
    private String isbn;

    private Integer pages;

    @Column(columnDefinition = "TEXT")
    private String summary;
}
