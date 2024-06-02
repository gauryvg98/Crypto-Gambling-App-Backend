package com.cryptoclyx.server.controller;

import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.ResponseObject;
import com.cryptoclyx.server.payload.res.UserProfileResponse;
import com.cryptoclyx.server.service.auth.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Log4j2
@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/current-user")
    public ResponseEntity getCurrentUser(UsernamePasswordAuthenticationToken token) {
        String curUserEmail = ((User)token.getPrincipal()).getEmail();
        UserProfileResponse userProfile = userService.getUserByEmail(curUserEmail);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("User profile info");
        resObj.setDetails(userProfile);
        return ResponseEntity.ok(resObj);
    }

}