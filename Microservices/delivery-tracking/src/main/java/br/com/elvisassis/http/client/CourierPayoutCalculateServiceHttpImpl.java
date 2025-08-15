package br.com.elvisassis.http.client;

import br.com.elvisassis.domain.exception.IntegrationCourierException;
import br.com.elvisassis.domain.service.CourierPayoutCalculationService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;

@ApplicationScoped
public class CourierPayoutCalculateServiceHttpImpl implements CourierPayoutCalculationService {

    @Inject
    @RestClient
    private CourierAPIClient courierAPIClient;

    @Override
    public Uni<BigDecimal> calculatePayout(Double distanceInKm) {

        return courierAPIClient
                .payoutCalculate(new CourierPayoutCalculateInput(distanceInKm))
                .onFailure().transform(e -> new IntegrationCourierException("Integration with Courier failed!"))
                .map(CourierPayoutResultModel::getPayoutFee);

    }
}
