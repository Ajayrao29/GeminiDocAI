package org.hartford.GeminiDocAI.service;

import org.hartford.GeminiDocAI.model.DocumentExtraction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationService {

    public List<String> validate(DocumentExtraction extraction) {
        List<String> errors = new ArrayList<>();

        if (extraction.getName() == null || extraction.getName().isEmpty()) {
            errors.add("Name is missing");
        }

        if (extraction.getPolicyNumber() == null || extraction.getPolicyNumber().isEmpty()) {
            errors.add("Policy Number is missing");
        } else if (!isNumeric(extraction.getPolicyNumber())) {
            errors.add("Policy Number must be numeric: " + extraction.getPolicyNumber());
        }

        if (extraction.getDate() == null || extraction.getDate().isEmpty()) {
            errors.add("Date is missing");
        } else {
            try {
                LocalDate.parse(extraction.getDate());
            } catch (DateTimeParseException e) {
                errors.add("Date format invalid (expected YYYY-MM-DD): " + extraction.getDate());
            }
        }

        return errors;
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        return str.matches("\\d+");
    }
}
