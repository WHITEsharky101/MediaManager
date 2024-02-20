package ru.whitesharky.mediamanager.adapter.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.whitesharky.mediamanager.adapter.dto.UserDto;

import ru.whitesharky.mediamanager.domain.User;
import ru.whitesharky.mediamanager.service.UserService;

import java.util.List;

@Controller
public class AuthController {

    private UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/")
    public String redirectHome(){
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model){
        User existingUser = userService.findUserByLogin(userDto.getLogin());

        if (userService.isLoginExists(userDto.getLogin())) {
            result.rejectValue("login", null,
                    "There is already an account with the same login");
        }

        if(userService.isEmailExists(userDto.getEmail())){
            result.rejectValue("email", null,
                    "There is already an account with the same email");
        }

        if(!userDto.getPassword().equals(userDto.getMatchingPassword())){
            result.rejectValue("password", null,
                    "Password doesn't match");
        }

        if(result.hasErrors()){
            model.addAttribute("user", userDto);
            return "/register";
        }

        userService.registerNewUserAccount(userDto);
        return "redirect:/register?success";
    }

    @GetMapping("/users")
    public String users(Model model){
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
}