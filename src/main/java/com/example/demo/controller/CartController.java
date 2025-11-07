package com.example.demo.controller;

import com.example.demo.dto.CartItemDTO;
import com.example.demo.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ✅ Add to cart directly (no auth)
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestParam String email,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.addToCart(email, productId, quantity));
    }

    // ✅ Get all cart items for a user
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart(@RequestParam String email) {
        return ResponseEntity.ok(cartService.getCartItemsDTO(email));
    }

    // ✅ Update cart item quantity
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateCart(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(email, id, quantity));
    }
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestParam String email) {
        cartService.clearCart(email);
        return ResponseEntity.ok("Cart cleared successfully after order placement");
    }


    // ✅ Remove from cart
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long id,
            @RequestParam String email) {
        return ResponseEntity.ok(cartService.removeFromCart(email, id));
    }

    // ✅ Merge cart items (for local + server sync)
    @PostMapping("/merge")
    public ResponseEntity<String> mergeCart(
            @RequestParam String email,
            @RequestBody List<Map<String, Object>> localCart) {

        for (Map<String, Object> item : localCart) {
            Long productId = Long.valueOf(item.get("id").toString());
            int quantity = Integer.parseInt(item.get("quantity").toString());
            cartService.addToCart(email, productId, quantity);
        }

        return ResponseEntity.ok("Cart merged");
    }
}
