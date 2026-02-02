package com.fenix.bibliotech;

import com.fenix.bibliotech.domain.Book;
import com.fenix.bibliotech.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BibliotechApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibliotechApplication.class, args);
	}

//	@Bean
//	CommandLineRunner run(BookRepository repository) {
//		return args -> {
//			Book book = Book.builder()
//					.title("O Código Limpo")
//					.author("Uncle Bob")
//					.isbn("123456")
//					.pages(464)
//					.build();
//			repository.save(book);
//			System.out.println("Livro SALVO COM SUCESSO NO POSTGRES");
//		};
//	}
}
