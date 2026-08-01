package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CarSearchRequestTest {

    @Test
    void rejectsNegativePriceMax() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CarSearchRequest.Filters(-1f))
                .withMessageContaining("priceMax");
    }

    @Test
    void rejectsNegativeMileageMax() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CarSearchRequest.Filters(null, null, -1, null))
                .withMessageContaining("mileageMax");
    }

    @Test
    void rejectsSortValueThatIsNotPriceAscOrMileageAsc() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CarSearchRequest("suv", null, "price_desc"))
                .withMessageContaining("sort");
    }
}
