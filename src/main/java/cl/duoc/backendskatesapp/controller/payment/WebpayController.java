package cl.duoc.backendskatesapp.controller.payment;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import cl.duoc.backendskatesapp.service.WebpayService;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionStatusResponse;

@RestController
@RequestMapping("/api/payments/webpay")
public class WebpayController {

    private final WebpayService webpayService;

    public WebpayController(WebpayService webpayService) {
        this.webpayService = webpayService;
    }

    @PostMapping({"/create", "/transactions"})
    public WebpayCreateResponse create(
            @Valid @org.springframework.web.bind.annotation.RequestBody WebpayCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return webpayService.create(request.amount(), request.sessionId(), jwt.getSubject(), request.items());
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnFromWebpay(
            @RequestParam(name = "token_ws", required = false) String token,
            @RequestParam(name = "TBK_TOKEN", required = false) String abortedToken) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(Map.of(
                    "status", "ABORTED",
                    "token", abortedToken == null ? "" : abortedToken));
        }
        return ResponseEntity.ok(webpayService.commit(token));
    }

    @PostMapping("/commit")
    public WebpayPaymentResponse commit(@RequestParam("token_ws") String token) {
        return webpayService.commit(token);
    }

    @GetMapping("/status")
    public WebpayPlusTransactionStatusResponse status(@RequestParam("token_ws") String token) {
        return webpayService.status(token);
    }
}
