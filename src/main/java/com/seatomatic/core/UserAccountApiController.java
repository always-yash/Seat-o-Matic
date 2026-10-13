package com.seatomatic.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAccountApiController {
    private final UserAccountService accounts;
    public UserAccountApiController(UserAccountService accounts) { this.accounts = accounts; }
    @GetMapping public List<UserAccountService.AccountView> all() { return accounts.list(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public UserAccountService.AccountView create(@Valid @RequestBody UserInput input) {
        return accounts.create(new UserAccountService.CreateAccount(input.username(), input.password(), input.role()));
    }
    @PatchMapping("/{id}/active")
    public UserAccountService.AccountView setActive(@PathVariable Long id, @Valid @RequestBody ActiveInput input) {
        return accounts.setActive(id, input.active());
    }
    public record UserInput(@NotBlank @Size(min = 3, max = 60) @Pattern(regexp = "[A-Za-z0-9._-]+") String username,
                            @NotBlank @Size(min = 12, max = 72) String password,
                            @NotNull UserAccount.Role role) { }
    public record ActiveInput(@NotNull Boolean active) { }
}
