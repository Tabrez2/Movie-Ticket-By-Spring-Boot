package com.tab.movie_ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.tab.movie_ticket.helper.AES;

@SpringBootApplication
public class MovieTicketApplication {

	public static void main(String[] args) {
//		System.out.println(AES.decrypt("mI+2OE7M2K0n6NyJm6CyNw==", "123"));
		
		SpringApplication.run(MovieTicketApplication.class, args);
	}
 
}
