package com.fenix.bibliotech.domain.helper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CustomerIdentifier {
    public static UUID generateId(String customerName) {
        return UUID.nameUUIDFromBytes(customerName.getBytes(StandardCharsets.UTF_8));
    }
}
