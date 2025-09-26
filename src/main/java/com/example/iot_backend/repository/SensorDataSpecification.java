// Tạo class SensorDataSpecification.java
package com.example.iot_backend.repository;

import com.example.iot_backend.model.SensorData;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SensorDataSpecification {

    public static Specification<SensorData> withSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Search in ID
            predicates.add(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(criteriaBuilder.toString(root.get("id"))),
                            searchPattern
                    )
            );

            // Search in temperature
            predicates.add(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(criteriaBuilder.toString(root.get("temperature"))),
                            searchPattern
                    )
            );

            // Search in humidity
            predicates.add(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(criteriaBuilder.toString(root.get("humidity"))),
                            searchPattern
                    )
            );

            // Search in lightLevel
            predicates.add(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(criteriaBuilder.toString(root.get("lightLevel"))),
                            searchPattern
                    )
            );

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<SensorData> withDateRange(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            if (start == null || end == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get("createdAt"), start, end);
        };
    }
}
