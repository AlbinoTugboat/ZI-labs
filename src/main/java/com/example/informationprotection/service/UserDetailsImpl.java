package com.example.informationprotection.service;

import com.example.informationprotection.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Boolean isActive;
    private Boolean isAccountExpired;
    private Boolean isAccountLocked;
    private Boolean isCredentialsExpired;
    private Boolean isDisabled;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
                           Boolean isActive,
                           Boolean isAccountExpired,
                           Boolean isAccountLocked,
                           Boolean isCredentialsExpired,
                           Boolean isDisabled,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.isAccountExpired = isAccountExpired;
        this.isAccountLocked = isAccountLocked;
        this.isCredentialsExpired = isCredentialsExpired;
        this.isDisabled = isDisabled;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(),
                user.getIsAccountExpired(),
                user.getIsAccountLocked(),
                user.getIsCredentialsExpired(),
                user.getIsDisabled(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }

    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return !Boolean.TRUE.equals(isAccountExpired); }
    @Override
    public boolean isAccountNonLocked() { return !Boolean.TRUE.equals(isAccountLocked); }
    @Override
    public boolean isCredentialsNonExpired() { return !Boolean.TRUE.equals(isCredentialsExpired); }
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive) && !Boolean.TRUE.equals(isDisabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
