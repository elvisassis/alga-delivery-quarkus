package br.com.elvisassis.domain.service;

import br.com.elvisassis.api.model.ContactPointInput;
import br.com.elvisassis.api.model.DeliveryInput;
import br.com.elvisassis.api.model.DeliveryResponseDTO;
import br.com.elvisassis.api.model.mapper.DeliveryMapper;
import br.com.elvisassis.domain.exception.DomainException;
import br.com.elvisassis.domain.model.ContactPoint;
import br.com.elvisassis.domain.model.Delivery;
import br.com.elvisassis.domain.repository.DeliveryRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class DeliveryPreparationService {

    @Inject
    private DeliveryRepository deliveryRepository;

    @Inject
    private DeliveryTimeEstimationService deliveryTimeEstimationService;

    @Inject
    private CourierPayoutCalculationService calculationPayout;

    @WithTransaction
    public Uni<DeliveryResponseDTO> draft(DeliveryInput input) {
        Delivery delivery = Delivery.draft();
        handlePreparation(input, delivery);
        return deliveryRepository.persist(delivery).replaceWith(delivery)
                .map(DeliveryMapper::toResponseDTO);
    }

    @WithTransaction
    public Uni<DeliveryResponseDTO> edit(UUID deliveryId, DeliveryInput input) {
        return deliveryRepository.findById(deliveryId)
                .onItem().ifNull().failWith(DomainException::new)
                .onItem().invoke(delivery -> {
                    delivery.removeItems();
                    handlePreparation(input, delivery);
                    delivery.place();
                })
                .map(DeliveryMapper::toResponseDTO);
    }

    private void handlePreparation(DeliveryInput input, Delivery delivery) {
        ContactPoint sender = toContactPoint(input.getSender());
        ContactPoint recipient = toContactPoint(input.getRecipient());

        DeliveryEstimate estimate = deliveryTimeEstimationService.estimate(sender, recipient);
        BigDecimal courierPayout = calculationPayout.calculatePayout(estimate.getDistanceInKm());

        var preparationDetails = new Delivery.PreparationDetails(
                sender,
                recipient,
                estimate.getEstimatedTime(),
                BigDecimal.valueOf(estimate.getDistanceInKm()),
                courierPayout
        );
        delivery.editPreparationDetails(preparationDetails);

        input.getItems().forEach(itemInput -> delivery.addItem(itemInput.getName(), itemInput.getQuantity()));
    }

    private ContactPoint toContactPoint(ContactPointInput input) {
        return new ContactPoint(
                input.getZipCode(),
                input.getStreet(),
                input.getNumber(),
                input.getComplement(),
                input.getName(),
                input.getPhone()
        );
    }
}
