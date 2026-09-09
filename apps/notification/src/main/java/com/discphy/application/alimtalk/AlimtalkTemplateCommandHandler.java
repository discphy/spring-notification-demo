package com.discphy.application.alimtalk;

import com.discphy.core.alimtalk.AlimtalkTemplate;
import com.discphy.core.alimtalk.AlimtalkTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlimtalkTemplateCommandHandler {

    private final AlimtalkTemplateRepository repository;

    @Transactional
    public void handle(AlimtalkTemplateCommand.Create command) {
        if (repository.findById(command.key()).isEmpty()) {
            repository.save(new AlimtalkTemplate(command.key(), command.content()));
        }
    }
}
