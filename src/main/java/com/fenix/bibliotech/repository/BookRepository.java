package com.fenix.bibliotech.repository;

import com.fenix.bibliotech.domain.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    boolean existsByIsbn(String isbn);

}
