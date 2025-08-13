package br.com.elvisassis.api.controller;

import br.com.elvisassis.api.model.CourierIdInput;
import br.com.elvisassis.api.model.DeliveryInput;
import br.com.elvisassis.api.model.DeliveryResponseDTO;
import br.com.elvisassis.api.model.PagedResult;
import br.com.elvisassis.api.model.mapper.DeliveryMapper;
import br.com.elvisassis.domain.model.DeliveryStatus;
import br.com.elvisassis.domain.service.DeliveryCheckpointService;
import br.com.elvisassis.domain.service.DeliveryPreparationService;
import br.com.elvisassis.domain.service.DeliveryService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/v1/deliveries")
@ApplicationScoped
public class DeliveryController {

    @Inject
    private DeliveryPreparationService deliveryPreparationService;

    @Inject
    private DeliveryMapper deliveryMapper;

    @Inject
    private DeliveryService deliveryService;

    @Inject
    private DeliveryCheckpointService deliveryCheckpointService;

    @POST
    public Uni<RestResponse<DeliveryResponseDTO>> draft(@Valid DeliveryInput input) {
        return deliveryPreparationService.draft(input)
                .map(dto -> RestResponse.status(RestResponse.Status.CREATED, dto));
    }

    @GET
    public Uni<PagedResult<DeliveryResponseDTO>> findAll(@QueryParam("page") int page,
                                                         @QueryParam("size") int size) {

        return deliveryService.findAll(page, size);
    }

    @PUT
    @Path("/{deliveryId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<DeliveryResponseDTO>> edit(@PathParam("deliveryId") UUID deliveryId, @Valid DeliveryInput input) {
        return deliveryPreparationService.edit(deliveryId, input)
                .map(dto -> RestResponse.ok(dto));
    }

    @GET
    @Path("/{deliveryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<DeliveryResponseDTO>> findById(@PathParam("deliveryId") UUID deliveryId) {
        return deliveryService.findById(deliveryId)
                .map(dto -> RestResponse.ok(dto));
    }

    @DELETE
    @Path("/{deliveryId}")
    public Uni<RestResponse<Void>> delete(@PathParam("deliveryId") UUID deliveryId) {
        return deliveryService.delete(deliveryId)
                .map(v -> RestResponse.noContent());
    }

    @POST
    @Path("/{deliveryId}/placement")
    public Uni<Void> place(@PathParam("deliveryId") UUID deliveryId) {
        return deliveryCheckpointService.place(deliveryId);
    }

    @POST
    @Path("/{deliveryId}/pickups")
    public Uni<Void> pickUp(@PathParam("deliveryId") UUID deliveryId, CourierIdInput input) {
        return deliveryCheckpointService.pickUp(deliveryId, input.getCourierId());
    }

    @POST
    @Path("/{deliveryId}/completion")
    public Uni<Void> complete(@PathParam("deliveryId") UUID deliveryId) {
        return deliveryCheckpointService.complete(deliveryId);
    }
}
