package com.tab.movie_ticket.controller;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tab.movie_ticket.dto.Movie;
import com.tab.movie_ticket.dto.Screen;
import com.tab.movie_ticket.dto.Seat;
import com.tab.movie_ticket.dto.Show;
import com.tab.movie_ticket.dto.Theatre;
import com.tab.movie_ticket.helper.AES;
import com.tab.movie_ticket.helper.EmailSendingHelper;
import com.tab.movie_ticket.repository.CustomerRepository;
import com.tab.movie_ticket.repository.MovieRepository;
import com.tab.movie_ticket.repository.ScreenRepository;
import com.tab.movie_ticket.repository.ShowRepository;
import com.tab.movie_ticket.repository.TheatreRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/theatre")
public class TheatreController {

	@Autowired
	Theatre theatre;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	TheatreRepository theatreRepository;

	@Autowired
	EmailSendingHelper emailSendingHelper;
	@Autowired
	ScreenRepository screenRepository;
	@Autowired
	MovieRepository movieRepository;
	@Autowired
	ShowRepository showRepository;

	@GetMapping("/signup")
	public String loadSignup(ModelMap map) {
		map.put("theatre", theatre);
		return "theatre-signup.html";
	}

	@PostMapping("/signup")
	public String signup(@Valid Theatre theatre, BindingResult result, HttpSession session) {
		if (!theatre.getPassword().equals(theatre.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "error.confirmPassword", "* Password Missmatch");
		}
		if (customerRepository.existsByEmail(theatre.getEmail())
				|| theatreRepository.existsByEmail(theatre.getEmail())) {
			result.rejectValue("email", "error.email", "* Account Already Exists");
		}
		if (customerRepository.existsByMobile(theatre.getMobile())
				|| theatreRepository.existsByMobile(theatre.getMobile())) {
			result.rejectValue("mobile", "error.mobile", "* Account Already Exists");
		}

		if (result.hasErrors()) {
			return "theatre-signup.html";
		} else {
			theatre.setPassword(AES.encrypt(theatre.getPassword(), "123"));
			theatre.setOtp(new Random().nextInt(100000, 1000000));
			emailSendingHelper.sendMailToTheatre(theatre);
			theatreRepository.save(theatre);
			session.setAttribute("success", "Otp Sent Success!!!");
			session.setAttribute("id", theatre.getId());
			return "redirect:/theatre/enter-otp";
		}
	}

	@GetMapping("/enter-otp")
	public String enterOtp(ModelMap map) {
		map.put("user", "theatre");
		return "enter-otp.html";
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam int id, @RequestParam int otp, HttpSession session) {
		Theatre theatre = theatreRepository.findById(id).orElseThrow();
		if (theatre.getOtp() == otp) {
			theatre.setVerified(true);
			theatreRepository.save(theatre);
			session.setAttribute("success", "Account Created Success");
			return "redirect:/login";
		} else {
			session.setAttribute("failure", "Invalid OTP! Try Again");
			return "redirect:/theatre/enter-otp";
		}
	}
	
	@GetMapping("/add-screen")
	public String addScreen(HttpSession session) {
		if(session.getAttribute("theatre")!=null)
			return "addScreen.html";
		else {
			session.setAttribute("failure", "invalid Session ,login again");
			return"redirect:/login";
		}
			
	}
	@PostMapping("/add-screen")
	public String addScreen(Screen screen,HttpSession session) {
		Theatre theatre = (Theatre) session.getAttribute("theatre");
		if(theatre!=null) {
			if(screenRepository.existsByName(screen.getName())) {
				session.setAttribute("failure","screen already exists");
				return "redirect:/";
			}
			else {
				
				 // we creating seats
				List<Seat>seats=new ArrayList<>();
				
				for(char i ='A';i<'A'+screen.getRow();i++) {
					for(int j=1;j<=screen.getColumn();j++) {
						Seat seat = new Seat();
						seat.setSeatNumber(i+""+j);
						seats.add(seat);
					}
				}
				screen.setSeats(seats);
				List<Screen>screens=theatre.getScreens();
				     screens.add(screen);
				theatreRepository.save(theatre);			     
				session.setAttribute("success", "screens and seats added");

				return "redirect:/";
			}
		}
		else {
			session.setAttribute("failure","invalid session, login again");
			return "redirect:/login";
		}
	}
	@GetMapping("/add-show")
	public String addShow(HttpSession session,ModelMap map) {
		Theatre theatre = (Theatre)session.getAttribute("theatre");
		if(theatre!=null) {
			
			List<Screen>screens=theatre.getScreens();
			List<Movie>movies=movieRepository.findAll();
			
			if(screens.isEmpty()) {
				session.setAttribute("failure", "No screens are available for adding shows");
			}
			if(movies.isEmpty()) {
				session.setAttribute("failure", "No movies are available for adding shows");
			}
				
			map.put("movies", movies);
			map.put("screens", screens);
			return "add-show";
			
		}
		else {
			session.setAttribute("failure", "Invalid session,login again");
			return "redirect:/login";
		}
	}
	@PostMapping("/add-show")
	public String addShow(HttpSession session,ModelMap map,Show show) {
		Theatre theatre = (Theatre) session.getAttribute("theatre");
		if(theatre!=null) {
			show.setMovie(movieRepository.findById(show.getMovie().getId()).orElseThrow());
			show.setScreen(screenRepository.findById(show.getScreen().getId()).orElseThrow());
			
			showRepository.save(show);
			
			session.setAttribute("success", "Show added successfully");
			return "redirect:/";
			
		}
		else {
			session.setAttribute("failure", "Invalid session,login again");
			return "redirect:/login";
		}
	}
	@GetMapping("/manage-show")
	public String manageShow(HttpSession session,ModelMap map) {
		
		Theatre theatre = (Theatre) session.getAttribute("theatre");
		if(theatre!=null) {
			List<Screen> screens = theatre.getScreens();
			List<Show>shows=showRepository.findByScreenIn(screens);
			if(shows.isEmpty()) {
				session.setAttribute("failure","No Show Added Yet");
				return "redirect:/";
				
			}
	      else{
				map.put("shows", shows);
				return "manage-show.html";
		  }			
	    }
		else {
			session.setAttribute("failure", "Invalid session,Login Again");
			return "redirect:/login";
		}
	}
	
	@GetMapping("/open-booking/{id}")
	public String openBooking(HttpSession session,@PathVariable int id) {
		Theatre theatre = (Theatre) session.getAttribute("theatre");
		if(theatre!=null) {
			Show show = showRepository.findById(id).orElseThrow();
			Screen screen = show.getScreen();
			int timing = show.getTiming();
			LocalDate movieDate=show.getMovie().getReleaseDate();
			List<Movie>movies=movieRepository.findByReleaseDate(movieDate);
			boolean flag=showRepository.existsByScreenAndTimingAndAvailableTrueAndMovieIn(screen,timing,movies);
			List<Seat>seats=screen.getSeats();
			
			if(flag) {
			  
				session.setAttribute("failure", "Already there is show  running");
				return "redirect:/theatre/manage-show";
			
		      }
		else {
			 show.setAvailable(true);
			 showRepository.save(show);
			 session.setAttribute("success", "Booking Open");
	         return "redirect:/theatre/manage-show";
	     	}
	}
		else {
			session.setAttribute("failure", "Invalid session,Login Again");
			return "redirect:/login";
			
		}
	
}
	
	@GetMapping("/close-booking/{id}")
	public String closeBooking(HttpSession session,@PathVariable int id) {
		Theatre theatre = (Theatre) session.getAttribute("theatre");
		if(theatre!=null) {
			Show show = showRepository.findById(id).orElseThrow();
			Screen screen = show.getScreen();
			List<Seat>seats=screen.getSeats();
			
			for(Seat seat : seats) {
				if(seat.isOccupied()) {
					session.setAttribute("failure", "Already Tickets Are Booked,Can not Cancel");
					return "redirect:/theatre/manage-show";
				}
			}
			
			show.setAvailable(false);
			showRepository.save(show);
			session.setAttribute("success", "Booking Closed");
			return "redirect:/theatre/manage-show";
			
			
		}
		else {
			session.setAttribute("failure", "Invalid Session,Login Again");
			return "redirect:/login";
			
		}
	}
	
}






















