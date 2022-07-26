package it.pagopa.pn.safestorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.pagopa.pn.safestorage.generated.rest.v1.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class SafeStorageService {

    private final Sha256Component sha256Calculator;
    private final SqsAsyncClient sqsClient;
    private final Path baseDir;
    private final String baseUploadUrl;
    private final String sqsQueueName;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final ObjectWriter eventWriter;

    private final ApplicationContext ctx;

    public SafeStorageService(
            Sha256Component sha256Calculator, SqsAsyncClient sqsClient, ApplicationContext ctx,
            @Value("${pn.mock.safe-storage.dir:${java.io.tmpdir}}") String tmpDir,
            @Value("${pn.mock.safe-storage.base-upload-url}") String baseUploadUrl,
            @Value("${pn.mock.safe-storage.sqs-name}") String sqsQueueName
    ) {
        this.sha256Calculator = sha256Calculator;
        this.sqsClient = sqsClient;
        this.ctx = ctx;
        baseDir = Paths.get( tmpDir );
        this.baseUploadUrl = baseUploadUrl + "/" ;
        this.sqsQueueName = sqsQueueName;

        ObjectMapper objMapper = new ObjectMapper();
        jsonWriter = objMapper.writerFor(FileCreationRequest.class);
        eventWriter = objMapper.writerFor(FileDownloadResponse.class);
        jsonReader = objMapper.readerFor(FileCreationRequest.class);
    }

    public FileCreationResponse createFile( FileCreationRequest req ) {
        try {
            String fileKey = UUID.randomUUID().toString();
            writeMetadata(req, fileKey);

            FileCreationResponse response = new FileCreationResponse();
            response.secret( fileKey )
                    .key( fileKey )
                    .uploadMethod( FileCreationResponse.UploadMethodEnum.PUT )
                    .uploadUrl( baseUploadUrl + fileKey );
            return response;
        }
        catch( IOException exc ) {
            throw new RuntimeException( exc );
        }
    }

    private void writeMetadata(FileCreationRequest req, String fileKey) throws IOException {
        String metadataJson = jsonWriter.writeValueAsString(req);
        Path metadataFile = baseDir.resolve(fileKey + ".json");
        Files.writeString( metadataFile, metadataJson );
    }


    public FileDownloadResponse getFile(String fileKey) {
        try {
            FileCreationRequest metadata = readMetadata(fileKey);

            FileDownloadResponse response = new FileDownloadResponse();

            Path file = baseDir.resolve( fileKey + ".pdf");

            try( InputStream inStrm = this.getFileStream( fileKey ) ) {
                response.key( fileKey )
                        .checksum( sha256Calculator.computeSha256( inStrm ))
                        .contentType(metadata.getContentType())
                        .documentType(metadata.getDocumentType())
                        .retentionUntil( new Date() )
                        .contentLength( BigDecimal.valueOf( Files.size( file )))
                        .documentStatus(metadata.getStatus())
                        .download(new FileDownloadInfo().url(baseUploadUrl + fileKey));
                return response;
            }


        }
        catch( IOException exc ) {
            throw new RuntimeException( exc );
        }
    }

    private FileCreationRequest readMetadata(String fileKey) throws IOException {
        Path metadataFile = baseDir.resolve(fileKey + ".json");
        String metadataJson = Files.readString(metadataFile);
        FileCreationRequest metadata = jsonReader.readValue(metadataJson);
        return metadata;
    }

    public OperationResultCodeResponse updateMetadata(String fileKey, UpdateFileMetadataRequest req) {
        try {
            FileCreationRequest metadata = readMetadata(fileKey);

            if( req.getStatus() != null ) {
                metadata.status( req.getStatus() );
            }

            writeMetadata( metadata, fileKey);

            return new OperationResultCodeResponse().resultCode("OK");
        }
        catch (IOException exc ) {
            throw new RuntimeException( exc );
        }
    }


    private InputStream getFileStream( String fileKey ) throws IOException {
        Path file = baseDir.resolve(fileKey + ".pdf");
        return Files.newInputStream( file );
    }


    public Mono<ResponseEntity<Object>> putFileContent( String key, ServerWebExchange exchange) {
        Path file = baseDir.resolve( key + ".pdf");
        return exchange.getRequest()
                .getBody()
                .collectList()
                .map( dataBlockList -> {
                    dataBlockList.forEach( dataBlock -> {
                        try {
                            byte[] bytes = StreamUtils.copyToByteArray( dataBlock.asInputStream() );
                            Files.write(file, bytes, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                        }
                        catch (IOException exc ) {
                            throw new RuntimeException(exc);
                        }
                    });
                    return ResponseEntity.ok("");
                })
                .flatMap( ( data ) ->
                    Mono.fromFuture( sendEvent( key ))
                            .map( str -> ResponseEntity.ok(Collections.singletonMap("result", str)) )
                );
    }

    public Mono<ResponseEntity<Resource>> getFileContent( String key ) {
        Path file = baseDir.resolve( key + ".pdf");
        return Mono.fromSupplier( () -> ResponseEntity.ok(ctx.getResource( "file://" + file.toString() )));
    }


    private CompletableFuture<String> sendEvent(String key) {
        try {
            FileDownloadResponse event = getFile(key);
            String eventJson = eventWriter.writeValueAsString(event);

            GetQueueUrlRequest getQueueUrl = GetQueueUrlRequest.builder().queueName( sqsQueueName).build();

            return sqsClient.getQueueUrl( getQueueUrl ).thenApplyAsync( (queueUrlResp ) -> {
                String queueUrl = queueUrlResp.queueUrl();

                return sqsClient.sendMessage(SendMessageRequest.builder()
                        .delaySeconds(5)
                        .messageBody(eventJson)
                        .queueUrl( queueUrl )
                        .build()
                    );
            })
            .thenApply( ( resp ) -> "OK" );
        }
        catch ( JsonProcessingException exc ) {
            throw new RuntimeException( exc );
        }
    }

}
