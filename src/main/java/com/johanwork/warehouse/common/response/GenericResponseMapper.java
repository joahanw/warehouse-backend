package com.johanwork.warehouse.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * This generic for automatic mapping between model and response
 * @param <M> Model Entity
 * @param <R> Request DTO
 * @param <S> Response DTO
 */
public interface GenericResponseMapper<M, R, S> {
    S mapEntityToResponse (M m);
    M mapRequestToEntity(R r);
    List<S> mapListEntityToListResponse(List<M> m);
    PageResponse<S> mapPageEntityToPageResponse(Page<M> m);

    default GenericResponse<Void> mapToGenericResponse(String message) {
        return new GenericResponse<>(null, message);
    }

    default GenericResponse<S> mapToGenericResponse(M m, String message) {
        var response = mapEntityToResponse(m);
        return new GenericResponse<>(response, message);
    }

    default GenericResponse<List<S>> mapToListGenericResponse(List<M> m, String message) {
        var response = mapListEntityToListResponse(m);
        return new GenericResponse<>(response, message);
    }

    default GenericResponse<PageResponse<S>> mapToPageGenericResponse(Page<M> m, String message) {
        var response = mapPageEntityToPageResponse(m);
        return new GenericResponse<>(response, message);
    }

}
