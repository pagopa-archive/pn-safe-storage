package it.pagopa.pn.safestorage;

import it.pagopa.pn.safestorage.generated.rest.v1.api.FileDownloadApi;
import it.pagopa.pn.safestorage.generated.rest.v1.api.FileMetadataUpdateApi;
import it.pagopa.pn.safestorage.generated.rest.v1.api.FileUploadApi;
import it.pagopa.pn.safestorage.generated.rest.v1.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class ApiController implements FileUploadApi, FileDownloadApi, FileMetadataUpdateApi {

    private final SafeStorageService svc;

    public ApiController(SafeStorageService svc) {
        this.svc = svc;
    }


    @Override
    public Mono<ResponseEntity<FileCreationResponse>> createFile(String xPagopaSafestorageCxId, Mono<FileCreationRequest> fileCreationRequest, ServerWebExchange exchange) {
        return fileCreationRequest.map( req ->
           ResponseEntity.ok( svc.createFile( req))
        );
    }



    @Override
    public Mono<ResponseEntity<FileDownloadResponse>> getFile(String fileKey, String xPagopaSafestorageCxId, Boolean metadataOnly, ServerWebExchange exchange) {
        return Mono.fromSupplier( () ->
            ResponseEntity.ok( svc.getFile( fileKey ) )
        );
    }


    @Override
    public Mono<ResponseEntity<OperationResultCodeResponse>> updateFileMetadata(String fileKey, String xPagopaSafestorageCxId, Mono<UpdateFileMetadataRequest> updateFileMetadataRequest, ServerWebExchange exchange) {
        return updateFileMetadataRequest.map( req -> ResponseEntity.ok(svc.updateMetadata( fileKey, req ) ));
    }


    @PutMapping("/files/{key}")
    public Mono<ResponseEntity<Object>> putFileContent( @PathVariable("key") String key, ServerWebExchange exchange) {
        return svc.putFileContent( key, exchange );
    }

    @GetMapping("/files/{key}")
    public Mono<ResponseEntity<Resource>> getFileContent(@PathVariable("key") String key ) {
        return svc.getFileContent( key );
    }

}
