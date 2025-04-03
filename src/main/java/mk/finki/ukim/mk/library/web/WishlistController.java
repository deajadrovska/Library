package mk.finki.ukim.mk.library.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayWishlistDto;
import mk.finki.ukim.mk.library.service.application.WishlistApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist", description = "Wishlist management endpoints")
public class WishlistController {

    private final WishlistApplicationService wishlistService;

    public WishlistController(WishlistApplicationService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active wishlist",
            description = "Retrieves the currently active wishlist for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist found",
                    content = @Content(schema = @Schema(implementation = DisplayWishlistDto.class))),
            @ApiResponse(responseCode = "404", description = "Wishlist not found")
    })
    public ResponseEntity<DisplayWishlistDto> getWishlist(Principal principal) {
        return wishlistService.getActiveWishlist(principal.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add book to wishlist",
            description = "Adds a book to the user's wishlist if available copies exist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book added successfully",
                    content = @Content(schema = @Schema(implementation = DisplayWishlistDto.class))),
            @ApiResponse(responseCode = "400", description = "Book not available or error occurred")
    })
    public ResponseEntity<?> addBookToWishlist(@PathVariable Long id, Principal principal) {
        try {
            return wishlistService.addBookToWishlist(principal.getName(), id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/remove/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove book from wishlist",
            description = "Removes a book from the user's wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book removed successfully",
                    content = @Content(schema = @Schema(implementation = DisplayWishlistDto.class))),
            @ApiResponse(responseCode = "400", description = "Error removing book")
    })
    public ResponseEntity<DisplayWishlistDto> removeBookFromWishlist(@PathVariable Long id, Principal principal) {
        return wishlistService.removeBookFromWishlist(principal.getName(), id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/borrow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Borrow all books in wishlist",
            description = "Borrows all books in the user's wishlist, decreasing available copies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All books borrowed successfully",
                    content = @Content(schema = @Schema(implementation = DisplayWishlistDto.class))),
            @ApiResponse(responseCode = "400", description = "Error borrowing books, possibly due to insufficient copies")
    })
    public ResponseEntity<?> borrowAllBooks(Principal principal) {
        try {
            return wishlistService.borrowAllBooks(principal.getName())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/books")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List books in wishlist",
            description = "Lists all books currently in the user's wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of books in wishlist",
                    content = @Content(schema = @Schema(implementation = DisplayBookDto.class)))
    })
    public List<DisplayBookDto> listBooksInWishlist(Principal principal) {
        return wishlistService.listBooksInWishlist(principal.getName());
    }
}