package com.discphy.presentation.user;

import com.discphy.application.user.UserCommand;
import com.discphy.application.user.UserCommandHandler;
import com.discphy.application.user.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandHandler handler;

    private final UserQueryService queries;

    public record Created(Long id) {

    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Created create(@RequestBody UserCommand.Create command) {
        return new Created(handler.handle(command));
    }

    @GetMapping("/{id}")
    public UserQueryService.Result get(@PathVariable Long id) {
        return queries.get(id);
    }
}
