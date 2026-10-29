package bg.fmi.uni.boomvox.service;

import bg.fmi.uni.boomvox.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class BaseService {

    protected String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return query.trim();
    }
}
