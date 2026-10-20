package com.eleven22exotics.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    // ---------- helpers ----------
    private ResultActions postJson(String url, String token, Object body) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(body));
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req);
    }

    private ResultActions patchAs(String url, String token) throws Exception {
        return mvc.perform(patch(url).header("Authorization", "Bearer " + token));
    }

    private String registerAndGetToken(String email) throws Exception {
        String res = postJson("/api/auth/register", null,
                Map.of("firstName", "Test", "lastName", "User", "email", email, "password", "Password123"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return om.readTree(res).get("token").asText();
    }

    private String adminToken() throws Exception {
        String res = postJson("/api/auth/login", null, Map.of("email", "admin@test.dk", "password", "AdminPass123!"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(res).get("token").asText();
    }

    private long firstCarId() throws Exception {
        JsonNode cars = om.readTree(mvc.perform(get("/api/cars")).andReturn().getResponse().getContentAsString());
        return cars.get(0).get("id").asLong();
    }

    private Map<String, Object> booking(long carId, int startOffset, int endOffset) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("carId", carId);
        m.put("startDate", LocalDate.now().plusDays(startOffset).toString());
        m.put("endDate", LocalDate.now().plusDays(endOffset).toString());
        return m;
    }

    private long createBooking(String token, long carId, int s, int e) throws Exception {
        String res = postJson("/api/bookings", token, booking(carId, s, e)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING")).andReturn().getResponse().getContentAsString();
        return om.readTree(res).get("id").asLong();
    }

    // ---------- authentication ----------
    @Test void registerAndLogin() throws Exception {
        registerAndGetToken("login1@test.dk");
        postJson("/api/auth/login", null, Map.of("email", "login1@test.dk", "password", "Password123"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test void duplicateEmailIsRejected() throws Exception {
        registerAndGetToken("dup1@test.dk");
        postJson("/api/auth/register", null, Map.of("firstName", "A", "lastName", "B", "email", "dup1@test.dk", "password", "Password123"))
                .andExpect(status().isConflict());
    }

    @Test void invalidLoginIsRejected() throws Exception {
        postJson("/api/auth/login", null, Map.of("email", "nobody@test.dk", "password", "whatever123")).andExpect(status().isUnauthorized());
    }

    @Test void weakPasswordFailsValidation() throws Exception {
        postJson("/api/auth/register", null, Map.of("firstName", "A", "lastName", "B", "email", "weak@test.dk", "password", "short"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    // ---------- cars ----------
    @Test void listsAllCarsPublicly() throws Exception {
        mvc.perform(get("/api/cars")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(12)));
    }

    @Test void filtersCarsByBrandAndCategory() throws Exception {
        mvc.perform(get("/api/cars?brand=BMW")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/cars?category=LUXURY_SUV")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test void getsCarByIdAndReturns404ForMissingCar() throws Exception {
        mvc.perform(get("/api/cars/" + firstCarId())).andExpect(status().isOk()).andExpect(jsonPath("$.brand").isNotEmpty());
        mvc.perform(get("/api/cars/999999")).andExpect(status().isNotFound());
    }

    // ---------- authorization ----------
    @Test void customerIsBlockedFromAdminEndpoints() throws Exception {
        String token = registerAndGetToken("blocked@test.dk");
        mvc.perform(get("/api/admin/bookings").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/bookings")).andExpect(status().isUnauthorized());
    }

    @Test void adminCanAddCar() throws Exception {
        Map<String, Object> car = new LinkedHashMap<>();
        car.put("brand", "McLaren"); car.put("model", "750S"); car.put("modelYear", 2024); car.put("category", "Supercar");
        car.put("dailyPrice", 8499); car.put("monthlyPrice", 159999); car.put("engine", "4.0L twin-turbo V8");
        car.put("horsepower", 740); car.put("transmission", "Automatic"); car.put("drivetrain", "RWD");
        car.put("fuelType", "Petrol"); car.put("seats", 2); car.put("doors", 2); car.put("zeroToOneHundred", 2.8); car.put("topSpeed", 332);
        postJson("/api/admin/cars", adminToken(), car).andExpect(status().isCreated()).andExpect(jsonPath("$.model").value("750S"));
        postJson("/api/admin/cars", registerAndGetToken("nocar@test.dk"), car).andExpect(status().isForbidden());
    }

    // ---------- bookings ----------
    @Test void bookingRequiresLogin() throws Exception {
        postJson("/api/bookings", null, booking(firstCarId(), 200, 203)).andExpect(status().isUnauthorized());
    }

    @Test void validBookingIsPendingWithServerCalculatedPrice() throws Exception {
        String token = registerAndGetToken("price@test.dk");
        postJson("/api/bookings", token, booking(firstCarId(), 300, 303)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING")).andExpect(jsonPath("$.totalPrice").value(3 * 2499));
    }

    @Test void invalidDatesAreRejected() throws Exception {
        String token = registerAndGetToken("dates@test.dk");
        postJson("/api/bookings", token, booking(firstCarId(), -2, 3)).andExpect(status().isBadRequest());
        postJson("/api/bookings", token, booking(firstCarId(), 10, 10)).andExpect(status().isBadRequest());
    }

    @Test void overlappingBookingIsRejectedButCancelledBookingsDoNotBlock() throws Exception {
        String token = registerAndGetToken("overlap@test.dk");
        long carId = firstCarId() + 1;
        long id = createBooking(token, carId, 400, 405);
        postJson("/api/bookings", token, booking(carId, 403, 408)).andExpect(status().isConflict());
        createBooking(token, carId, 405, 407);   // back-to-back is allowed
        patchAs("/api/bookings/" + id + "/cancel", token).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        createBooking(token, carId, 400, 405);   // cancelled dates are free again
    }

    @Test void adminApprovesAndRejectsBookings() throws Exception {
        String token = registerAndGetToken("approve@test.dk"), admin = adminToken();
        long carId = firstCarId() + 2;
        long a = createBooking(token, carId, 500, 502), b = createBooking(token, carId, 510, 512);
        patchAs("/api/admin/bookings/" + a + "/approve", admin).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        patchAs("/api/admin/bookings/" + b + "/reject", admin).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        patchAs("/api/admin/bookings/" + a + "/approve", admin).andExpect(status().isConflict());
        mvc.perform(get("/api/bookings/my").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
