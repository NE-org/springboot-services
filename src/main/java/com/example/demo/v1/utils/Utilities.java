package com.example.demo.v1.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Utilities {
    public static boolean isValidDateFormat(String dob) {
        try {
            LocalDate parsedDate = LocalDate.parse(dob, DateTimeFormatter.ISO_DATE);
            // Check if parsed date is not in the future
            LocalDate currentDate = LocalDate.now();
            if (parsedDate.isAfter(currentDate)) {
                return false;  // Date is in the future
            }

            return true;  // Date is valid and not in the future
        } catch (DateTimeParseException e) {
            return false;  // Invalid date format
        }
    }

    public static String generateAccountNumber(String email) {
        String prefix = "BIS24";

        // Generate a hash of the email
        String hash = hashEmailToNumber(email);

        // Take a substring of the hash to ensure the account number is within the required length
        String uniquePart = hash.substring(0, 2); // Taking the first 6 characters for example

        return prefix + uniquePart;
    }

    private static String hashEmailToNumber(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(email.getBytes());
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(10); // Convert hash to a base-10 number
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
