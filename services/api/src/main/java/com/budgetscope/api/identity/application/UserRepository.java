package com.budgetscope.api.identity.application;

import com.budgetscope.api.identity.domain.EmailAddress;
import com.budgetscope.api.identity.domain.User;

public interface UserRepository {
    boolean existsByEmail(EmailAddress email);

    User save(User user) throws DuplicateUserEmailException;
}
