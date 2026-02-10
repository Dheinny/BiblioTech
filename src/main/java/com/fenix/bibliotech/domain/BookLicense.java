package com.fenix.bibliotech.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name="book_license")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookLicense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String licenseCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Builder.Default
    private Boolean active = true;
}
