package com.tab.movie_ticket.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
@Data
@Entity
public class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private  int id;
	private String name;
	private	String genre;
	private String description;
	private int duration;
	private String cast_name;
	private String movie_poster;
	private String trailerLink;
	private LocalDate releaseDate;

}
