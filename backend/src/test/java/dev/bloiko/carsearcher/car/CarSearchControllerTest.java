package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for {@code GET /api/cars/{id}}. See
 * docs/listing-detail-page/design.md — "Backend endpoint" and
 * requirements.md R1.1/R1.2. {@link CarLookupService} is mocked; this test
 * only proves the controller maps {@link Optional#of}/{@link Optional#empty()}
 * to 200/404 and serializes the full {@link Car}, not the OpenSearch lookup
 * itself (that's {@link CarLookupServiceTest}'s job).
 */
@WebMvcTest(CarSearchController.class)
class CarSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarSearchService carSearchService;

    @MockBean
    private CarLookupService carLookupService;

    @Test
    void getReturnsCarWithOkStatusWhenFound() throws Exception {
        Car car = new Car("car-1", "Toyota", "RAV4", 2021, 27_500f, 18_000, "Automatic",
                "A reliable family SUV", List.of("https://example.com/photo.jpg"));
        when(carLookupService.findById("car-1")).thenReturn(Optional.of(car));

        mockMvc.perform(get("/api/cars/car-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("car-1"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("RAV4"))
                .andExpect(jsonPath("$.description").value("A reliable family SUV"));
    }

    @Test
    void getReturnsNotFoundWhenCarDoesNotExist() throws Exception {
        when(carLookupService.findById("missing-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cars/missing-id"))
                .andExpect(status().isNotFound());
    }
}
