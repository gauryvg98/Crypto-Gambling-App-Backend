package com.cryptoclyx.server.service.auth;

import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.payload.res.UserProfileResponse;
import com.cryptoclyx.server.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private ModelMapper modelMapper;


    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String str) throws UsernameNotFoundException {
        return userRepository.findByEmail(str);
    }

    public UserProfileResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return modelMapper.map(user, UserProfileResponse.class);
    }

    public List<UserProfileResponse> getAdmins() {
        List<User> admins = userRepository.getAdmins();
        return admins.stream().map(user -> modelMapper.map(user, UserProfileResponse.class)).collect(toList());
    }
}
