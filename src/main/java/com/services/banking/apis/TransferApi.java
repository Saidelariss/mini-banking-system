package com.services.banking.apis;


import com.services.banking.dtos.request.TransferRequest;
import com.services.banking.dtos.response.TransferResponse;
import com.services.banking.services.TransferService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/transfers")
public class TransferApi {
    private final TransferService transferService;

    @PostMapping
    TransferResponse doTransfer(@RequestBody @Valid TransferRequest request) {
        return transferService.doTransfer(request);
    }
}
