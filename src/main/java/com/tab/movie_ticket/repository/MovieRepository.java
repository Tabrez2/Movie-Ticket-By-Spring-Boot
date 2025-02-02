package com.tab.movie_ticket.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tab.movie_ticket.dto.Movie;

public interface MovieRepository extends JpaRepository<Movie,Integer> {

	List<Movie> findByReleaseDate(LocalDate movieDate);

}
