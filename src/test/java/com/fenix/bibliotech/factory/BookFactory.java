package com.fenix.bibliotech.factory;

import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.dto.request.BookRequestDTO;

public class BookFactory {
    public static Book BookValid1() {
        return Book.builder()
                .title("Memórias Póstumas de Brás Cubas")
                .author("Machado de Assis")
                .isbn("9788572327581")
                .pages(240)
                .build();
    }

    public static Book BookValid2() {
        return Book.builder()
                .title("Capitães da Areia")
                .author("Jorge Amado")
                .isbn("9788535911695")
                .pages(280)
                .build();
    }

    public static Book BookValid3() {
        return Book.builder()
                .title("A Hora da Estrela")
                .author("Clarice Lispector")
                .isbn("9788532508102")
                .pages(88)
                .build();
    }

    public static BookRequestDTO bookToCreateValid () {
        return BookRequestDTO.builder()
                .title("Harry Potter e a Pedra Filosofal")
                .author("J.K. Rowling")
                .isbn("9788532511010")
                .pages(223)
                .build();
    }
}
